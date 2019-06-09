package io.confluent.developer;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RatedMovie;
import io.confluent.developer.avro.Rating;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import static org.junit.Assert.*;

import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class JoinStreamToTableTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    public SpecificAvroSerializer<Movie> makeMovieSerializer(Properties envProps) {
        SpecificAvroSerializer<Movie> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    public SpecificAvroSerializer<Rating> makeRatingSerializer(Properties envProps) {
        SpecificAvroSerializer<Rating> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    public SpecificAvroDeserializer<RatedMovie> makeRatedMovieDeserializer(Properties envProps) {
        SpecificAvroDeserializer<RatedMovie> deserializer = new SpecificAvroDeserializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    private List<RatedMovie> readOutputTopic(TopologyTestDriver testDriver,
                                             String topic,
                                             Deserializer<Long> keyDeserializer,
                                             SpecificAvroDeserializer<RatedMovie> makeRatedMovieDeserializer) {
        List<RatedMovie> results = new ArrayList<>();

        while (true) {
            ProducerRecord<Long, RatedMovie> record = testDriver.readOutput(topic, keyDeserializer, makeRatedMovieDeserializer);

            if (record != null) {
                results.add(record.value());
            } else {
                break;
            }
        }

        return results;
    }

    @Test
    public void testJoin() throws IOException {
        JoinStreamToTable jst = new JoinStreamToTable();
        Properties envProps = jst.loadEnvProperties(TEST_CONFIG_FILE);
        Properties streamProps = jst.buildStreamsProperties(envProps);

        String tableTopic = envProps.getProperty("table.topic.name");
        String streamTopic = envProps.getProperty("stream.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        Topology topology = jst.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<Long> keySerializer = Serdes.Long().serializer();
        SpecificAvroSerializer<Movie> movieSerializer = makeMovieSerializer(envProps);
        SpecificAvroSerializer<Rating> ratingSerializer = makeRatingSerializer(envProps);

        Deserializer<Long> longDeserializer = Serdes.Long().deserializer();
        SpecificAvroDeserializer<RatedMovie> valueDeserializer = makeRatedMovieDeserializer(envProps);

        ConsumerRecordFactory<Long, Movie> movieFactory = new ConsumerRecordFactory<>(keySerializer, movieSerializer);
        ConsumerRecordFactory<Long, Rating> ratingFactory = new ConsumerRecordFactory<>(keySerializer, ratingSerializer);

        List<Movie> movies = new ArrayList<>();
        movies.add(Movie.newBuilder().setId(294).setTitle("Die Hard").setReleaseYear(1988).build());
        movies.add(Movie.newBuilder().setId(354).setTitle("Tree of Life").setReleaseYear(2011).build());

        List<Rating> ratings = new ArrayList<>();
        ratings.add(Rating.newBuilder().setId(294).setRating(8.1).build());
        ratings.add(Rating.newBuilder().setId(294).setRating(8.2).build());
        ratings.add(Rating.newBuilder().setId(354).setRating(9.2).build());
        ratings.add(Rating.newBuilder().setId(354).setRating(9.6).build());

        List<RatedMovie> ratedMovies = new ArrayList<>();
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setRating(8.1).build());
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setRating(8.2).build());
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setRating(9.2).build());
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setRating(9.6).build());

        for(Movie movie : movies) {
            testDriver.pipeInput(movieFactory.create(tableTopic, movie.getId(), movie));
        }

        for(Rating rating: ratings) {
            testDriver.pipeInput(ratingFactory.create(streamTopic, rating.getId(), rating));
        }

        List<RatedMovie> actualOutput = readOutputTopic(testDriver, outputTopic, longDeserializer, valueDeserializer);

        assertEquals(ratedMovies, actualOutput);
    }

}
