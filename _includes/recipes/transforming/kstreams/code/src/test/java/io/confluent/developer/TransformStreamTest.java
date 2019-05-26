package io.confluent.developer;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RawMovie;
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

public class TransformStreamTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    public SpecificAvroSerializer<RawMovie> makeSerializer(Properties envProps) {
        SpecificAvroSerializer<RawMovie> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    public SpecificAvroDeserializer<Movie> makeDeserializer(Properties envProps) {
        SpecificAvroDeserializer<Movie> deserializer = new SpecificAvroDeserializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    private List<Movie> readOutputTopic(TopologyTestDriver testDriver,
                                              String topic,
                                              Deserializer<String> keyDeserializer,
                                              SpecificAvroDeserializer<Movie> valueDeserializer) {
        List<Movie> results = new ArrayList<>();

        while (true) {
            ProducerRecord<String, Movie> record = testDriver.readOutput(topic, keyDeserializer, valueDeserializer);

            if (record != null) {
                results.add(record.value());
            } else {
                break;
            }
        }

        return results;
    }

    @Test
    public void testMovieConverter() {
      TransformStream ts = new TransformStream();
      Movie movie;

      movie = ts.convertRawMovie(new RawMovie(294L, "Tree of Life::2011", "drama"));
      assertNotNull(movie);
      assertEquals(294, movie.getId().intValue());
      assertEquals("Tree of Life", movie.getTitle());
      assertEquals(2011, movie.getReleaseYear().intValue());
      assertEquals("drama", movie.getGenre());
    }


    @Test
    public void testTransformStream() throws IOException {
        TransformStream ts = new TransformStream();
        Properties envProps = ts.loadEnvProperties(TEST_CONFIG_FILE);
        Properties streamProps = ts.buildStreamsProperties(envProps);

        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        Topology topology = ts.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<String> keySerializer = Serdes.String().serializer();
        SpecificAvroSerializer<RawMovie> valueSerializer = makeSerializer(envProps);

        Deserializer<String> keyDeserializer = Serdes.String().deserializer();
        SpecificAvroDeserializer<Movie> valueDeserializer = makeDeserializer(envProps);

        ConsumerRecordFactory<String, RawMovie> inputFactory = new ConsumerRecordFactory<>(keySerializer, valueSerializer);

        RawMovie rawDieHard = RawMovie.newBuilder().setId(294).setTitle("Die Hard::1988").setGenre("action").build();
        RawMovie rawTreeOfLife = RawMovie.newBuilder().setId(354).setTitle("Tree of Life::2011").setGenre("drama").build();;

        List<RawMovie> input = new ArrayList<>();
        input.add(rawDieHard);
        input.add(rawTreeOfLife);

        Movie dieHard = Movie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setGenre("action").build();
        Movie treeOfLife = Movie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setGenre("drama").build();;

        List<Movie> expectedOutput = new ArrayList<>();
        expectedOutput.add(dieHard);
        expectedOutput.add(treeOfLife);

        for (RawMovie rawMovie : input) {
            testDriver.pipeInput(inputFactory.create(inputTopic, rawMovie.getTitle(), rawMovie));
        }

        List<Movie> actualOutput = readOutputTopic(testDriver, outputTopic, keyDeserializer, valueDeserializer);

        assertEquals(expectedOutput, actualOutput);
    }

}
