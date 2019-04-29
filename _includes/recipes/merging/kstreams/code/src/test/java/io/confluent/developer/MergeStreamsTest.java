package io.confluent.developer;

import io.confluent.developer.avro.SongEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class MergeStreamsTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    public SpecificAvroSerializer<SongEvent> makeSerializer(Properties envProps) {
        SpecificAvroSerializer<SongEvent> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    public SpecificAvroDeserializer<SongEvent> makeDeserializer(Properties envProps) {
        SpecificAvroDeserializer<SongEvent> deserializer = new SpecificAvroDeserializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    @Test
    public void testMergeStreams() throws IOException {
        MergeStreams ms = new MergeStreams();
        Properties envProps = ms.loadEnvProperties(TEST_CONFIG_FILE);
        Properties streamProps = ms.buildStreamsProperties(envProps);

        String rockTopic = envProps.getProperty("input.rock.topic.name");
        String classicalTopic = envProps.getProperty("input.classical.topic.name");
        String allGenresTopic = envProps.getProperty("output.topic.name");

        Topology topology = ms.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<String> keySerializer = Serdes.String().serializer();
        SpecificAvroSerializer<SongEvent> valueSerializer = makeSerializer(envProps);

        Deserializer<String> keyDeserializer = Serdes.String().deserializer();
        SpecificAvroDeserializer<SongEvent> valueDeserializer = makeDeserializer(envProps);

        ConsumerRecordFactory<String, SongEvent> inputFactory = new ConsumerRecordFactory<>(keySerializer, valueSerializer);

        List<SongEvent> rockSongs = new ArrayList<>();
        List<SongEvent> classicalSongs = new ArrayList<>();

        rockSongs.add(SongEvent.newBuilder().setArtist("Metallica").setTitle("Fade to Black").build());
        rockSongs.add(SongEvent.newBuilder().setArtist("Smashing Pumpkins").setTitle("Today").build());
        rockSongs.add(SongEvent.newBuilder().setArtist("Pink Floyd").setTitle("Another Brick in the Wall").build());
        rockSongs.add(SongEvent.newBuilder().setArtist("Van Halen").setTitle("Jump").build());
        rockSongs.add(SongEvent.newBuilder().setArtist("Led Zeppelin").setTitle("Kashmir").build());

        classicalSongs.add(SongEvent.newBuilder().setArtist("Wolfgang Amadeus Mozart").setTitle("The Magic Flute").build());
        classicalSongs.add(SongEvent.newBuilder().setArtist("Johann Pachelbel").setTitle("Canon").build());
        classicalSongs.add(SongEvent.newBuilder().setArtist("Ludwig van Beethoven").setTitle("Symphony No. 5").build());
        classicalSongs.add(SongEvent.newBuilder().setArtist("Edward Elgar").setTitle("Pomp and Circumstance").build());

        for (SongEvent song : rockSongs) {
            testDriver.pipeInput(inputFactory.create(rockTopic, song.getArtist(), song));
        }

        for (SongEvent song : classicalSongs) {
            testDriver.pipeInput(inputFactory.create(classicalTopic, song.getArtist(), song));
        }

        List<SongEvent> actualOutput = new ArrayList<>();
        while (true) {
            ProducerRecord<String, SongEvent> record = testDriver.readOutput(allGenresTopic, keyDeserializer, valueDeserializer);

            if (record != null) {
                actualOutput.add(record.value());
            } else {
                break;
            }
        }

        List<SongEvent> expectedOutput = new ArrayList<>();
        expectedOutput.addAll(rockSongs);
        expectedOutput.addAll(classicalSongs);

        Assert.assertEquals(expectedOutput, actualOutput);
    }

}
