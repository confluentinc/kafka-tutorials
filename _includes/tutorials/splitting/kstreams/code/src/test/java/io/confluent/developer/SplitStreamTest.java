package io.confluent.developer;

import io.confluent.developer.avro.ActingEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
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

public class SplitStreamTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    public SpecificAvroSerializer<ActingEvent> makeSerializer(Properties envProps) {
        SpecificAvroSerializer<ActingEvent> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    public SpecificAvroDeserializer<ActingEvent> makeDeserializer(Properties envProps) {
        SpecificAvroDeserializer<ActingEvent> deserializer = new SpecificAvroDeserializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    private List<ActingEvent> readOutputTopic(TopologyTestDriver testDriver,
                                              String topic,
                                              Deserializer<String> keyDeserializer,
                                              SpecificAvroDeserializer<ActingEvent> valueDeserializer) {
        List<ActingEvent> results = new ArrayList<>();

        while (true) {
            ProducerRecord<String, ActingEvent> record = testDriver.readOutput(topic, keyDeserializer, valueDeserializer);

            if (record != null) {
                results.add(record.value());
            } else {
                break;
            }
        }

        return results;
    }

    @Test
    public void testSplitStream() throws IOException {
        SplitStream ss = new SplitStream();
        Properties envProps = ss.loadEnvProperties(TEST_CONFIG_FILE);
        Properties streamProps = ss.buildStreamsProperties(envProps);

        String inputTopic = envProps.getProperty("input.topic.name");
        String outputDramaTopic = envProps.getProperty("output.drama.topic.name");
        String outputFantasyTopic = envProps.getProperty("output.fantasy.topic.name");
        String outputOtherTopic = envProps.getProperty("output.other.topic.name");

        Topology topology = ss.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<String> keySerializer = Serdes.String().serializer();
        SpecificAvroSerializer<ActingEvent> valueSerializer = makeSerializer(envProps);

        Deserializer<String> keyDeserializer = Serdes.String().deserializer();
        SpecificAvroDeserializer<ActingEvent> valueDeserializer = makeDeserializer(envProps);

        ConsumerRecordFactory<String, ActingEvent> inputFactory = new ConsumerRecordFactory<>(keySerializer, valueSerializer);

        ActingEvent streep = ActingEvent.newBuilder()
                .setName("Meryl Streep").setTitle("The Iron Lady").setGenre("drama").build();
        ActingEvent smith = ActingEvent.newBuilder()
                .setName("Will Smith").setTitle("Men in Black").setGenre("comedy").build();
        ActingEvent damon = ActingEvent.newBuilder()
                .setName("Matt Damon").setTitle("The Martian").setGenre("drama").build();
        ActingEvent garland = ActingEvent.newBuilder()
                .setName("Judy Garland").setTitle("The Wizard of Oz").setGenre("fantasy").build();
        ActingEvent aniston = ActingEvent.newBuilder()
                .setName("Jennifer Aniston").setTitle("Office Space").setGenre("comedy").build();
        ActingEvent murray = ActingEvent.newBuilder()
                .setName("Bill Murray").setTitle("Ghostbusters").setGenre("fantasy").build();
        ActingEvent bale = ActingEvent.newBuilder()
                .setName("Christian Bale").setTitle("The Dark Knight").setGenre("crime").build();
        ActingEvent dern = ActingEvent.newBuilder()
                .setName("Laura Dern").setTitle("Jurassic Park").setGenre("fantasy").build();
        ActingEvent reeves = ActingEvent.newBuilder()
                .setName("Keanu Reeves").setTitle("The Matrix").setGenre("fantasy").build();
        ActingEvent crowe = ActingEvent.newBuilder()
                .setName("Russell Crowe").setTitle("Gladiator").setGenre("drama").build();
        ActingEvent keaton = ActingEvent.newBuilder()
                .setName("Diane Keaton").setTitle("The Godfather: Part II").setGenre("crime").build();

        List<ActingEvent> input = new ArrayList<>();
        input.add(streep);
        input.add(smith);
        input.add(damon);
        input.add(garland);
        input.add(aniston);
        input.add(murray);
        input.add(bale);
        input.add(dern);
        input.add(reeves);
        input.add(crowe);
        input.add(keaton);

        List<ActingEvent> expectedDrama = new ArrayList<>();
        expectedDrama.add(streep);
        expectedDrama.add(damon);
        expectedDrama.add(crowe);

        List<ActingEvent> expectedFantasy = new ArrayList<>();
        expectedFantasy.add(garland);
        expectedFantasy.add(murray);
        expectedFantasy.add(dern);
        expectedFantasy.add(reeves);

        List<ActingEvent> expectedOther = new ArrayList<>();
        expectedOther.add(smith);
        expectedOther.add(aniston);
        expectedOther.add(bale);
        expectedOther.add(keaton);

        for (ActingEvent event : input) {
            testDriver.pipeInput(inputFactory.create(inputTopic, event.getName(), event));
        }

        List<ActingEvent> actualDrama = readOutputTopic(testDriver, outputDramaTopic, keyDeserializer, valueDeserializer);
        List<ActingEvent> actualFantasy = readOutputTopic(testDriver, outputFantasyTopic, keyDeserializer, valueDeserializer);
        List<ActingEvent> actualOther = readOutputTopic(testDriver, outputOtherTopic, keyDeserializer, valueDeserializer);

        Assert.assertEquals(expectedDrama, actualDrama);
        Assert.assertEquals(expectedFantasy, actualFantasy);
        Assert.assertEquals(expectedOther, actualOther);
    }

}