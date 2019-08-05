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

    private SpecificAvroSerializer<Movie> makeMovieSerializer(Properties envProps) {
        SpecificAvroSerializer<Movie> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    private SpecificAvroSerializer<Rating> makeRatingSerializer(Properties envProps) {
        SpecificAvroSerializer<Rating> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    private SpecificAvroDeserializer<RatedMovie> makeRatedMovieDeserializer(Properties envProps) {
        SpecificAvroDeserializer<RatedMovie> deserializer = new SpecificAvroDeserializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    private List<RatedMovie> readOutputTopic(TopologyTestDriver testDriver,
                                             String topic,
                                             Deserializer<String> keyDeserializer,
                                             SpecificAvroDeserializer<RatedMovie> makeRatedMovieDeserializer) {
        List<RatedMovie> results = new ArrayList<>();

        while (true) {
            ProducerRecord<String, RatedMovie> record = testDriver.readOutput(topic, keyDeserializer, makeRatedMovieDeserializer);

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

        String tableTopic = envProps.getProperty("movie.topic.name");
        String streamTopic = envProps.getProperty("rating.topic.name");
        String outputTopic = envProps.getProperty("rated.movies.topic.name");

        Topology topology = jst.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<String> keySerializer = Serdes.String().serializer();
        SpecificAvroSerializer<Movie> movieSerializer = makeMovieSerializer(envProps);
        SpecificAvroSerializer<Rating> ratingSerializer = makeRatingSerializer(envProps);

        Deserializer<String> stringDeserializer = Serdes.String().deserializer();
        SpecificAvroDeserializer<RatedMovie> valueDeserializer = makeRatedMovieDeserializer(envProps);

        ConsumerRecordFactory<String, Movie> movieFactory = new ConsumerRecordFactory<>(keySerializer, movieSerializer);
        ConsumerRecordFactory<String, Rating> ratingFactory = new ConsumerRecordFactory<>(keySerializer, ratingSerializer);


        List<Movie> movies = new ArrayList<>();
        movies.add(Movie.newBuilder().setId(294).setTitle("Die Hard").setReleaseYear(1988).build());
        movies.add(Movie.newBuilder().setId(354).setTitle("Tree of Life").setReleaseYear(2011).build());
        movies.add(Movie.newBuilder().setId(782).setTitle("A Walk in the Clouds").setReleaseYear(1998).build());
        movies.add(Movie.newBuilder().setId(128).setTitle("The Big Lebowski").setReleaseYear(1998).build());
        movies.add(Movie.newBuilder().setId(780).setTitle("Super Mario Bros.").setReleaseYear(1993).build());

        List<Rating> ratings = new ArrayList<>();
        ratings.add(Rating.newBuilder().setId(294).setRating(8.2).build());
        ratings.add(Rating.newBuilder().setId(294).setRating(8.5).build());
        ratings.add(Rating.newBuilder().setId(354).setRating(9.9).build());
        ratings.add(Rating.newBuilder().setId(354).setRating(9.7).build());
        ratings.add(Rating.newBuilder().setId(782).setRating(7.8).build());
        ratings.add(Rating.newBuilder().setId(782).setRating(7.7).build());
        ratings.add(Rating.newBuilder().setId(128).setRating(8.7).build());
        ratings.add(Rating.newBuilder().setId(128).setRating(8.4).build());
        ratings.add(Rating.newBuilder().setId(780).setRating(2.1).build());

        List<RatedMovie> ratedMovies = new ArrayList<>();
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setRating(8.2).build());
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setRating(8.5).build());
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setRating(9.9).build());
        ratedMovies.add(RatedMovie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setRating(9.7).build());
        ratedMovies.add(RatedMovie.newBuilder().setId(782).setTitle("A Walk in the Clouds").setReleaseYear(1998).setRating(7.8).build());
        ratedMovies.add(RatedMovie.newBuilder().setId(782).setTitle("A Walk in the Clouds").setReleaseYear(1998).setRating(7.7).build());
        ratedMovies.add(RatedMovie.newBuilder().setId(128).setTitle("The Big Lebowski").setReleaseYear(1998).setRating(8.7).build());
        ratedMovies.add(RatedMovie.newBuilder().setId(128).setTitle("The Big Lebowski").setReleaseYear(1998).setRating(8.4).build());
        ratedMovies.add(RatedMovie.newBuilder().setId(780).setTitle("Super Mario Bros.").setReleaseYear(1993).setRating(2.1).build());

        for(Movie movie : movies) {
            testDriver.pipeInput(movieFactory.create(tableTopic, movie.getId().toString(), movie));
        }

        for(Rating rating: ratings) {
            testDriver.pipeInput(ratingFactory.create(streamTopic, rating.getId().toString(), rating));
        }

        List<RatedMovie> actualOutput = readOutputTopic(testDriver, outputTopic, stringDeserializer, valueDeserializer);

        assertEquals(ratedMovies, actualOutput);
    }

}
