package io.confluent.developer;

import io.confluent.developer.avro.User;
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

    public SpecificAvroSerializer<User> makeSerializer(Properties envProps) {
        SpecificAvroSerializer<User> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    public SpecificAvroDeserializer<User> makeDeserializer(Properties envProps) {
        SpecificAvroDeserializer<User> deserializer = new SpecificAvroDeserializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    private List<User> readOutputTopic(TopologyTestDriver testDriver, String topic, Deserializer<String> keyDeserializer,
                                       SpecificAvroDeserializer<User> valueDeserializer) {
        List<User> results = new ArrayList<>();

        while (true) {
            ProducerRecord<String, User> record = testDriver.readOutput(topic, keyDeserializer, valueDeserializer);

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
        String outputUsaTopic = envProps.getProperty("output.usa.topic.name");
        String outputMexTopic = envProps.getProperty("output.mex.topic.name");
        String outputGerTopic = envProps.getProperty("output.ger.topic.name");
        String outputOtherTopic = envProps.getProperty("output.other.topic.name");

        Topology topology = ss.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<String> keySerializer = Serdes.String().serializer();
        SpecificAvroSerializer<User> valueSerializer = makeSerializer(envProps);

        Deserializer<String> keyDeserializer = Serdes.String().deserializer();
        SpecificAvroDeserializer<User> valueDeserializer = makeDeserializer(envProps);

        ConsumerRecordFactory<String, User> inputFactory = new ConsumerRecordFactory<>(keySerializer, valueSerializer);

        User michael = User.newBuilder().setName("michael").setCountry("united states").build();
        User tim = User.newBuilder().setName("tim").setCountry("mexico").build();
        User jill = User.newBuilder().setName("jill").setCountry("germany").build();
        User lucas = User.newBuilder().setName("lucas").setCountry("australia").build();
        User steve = User.newBuilder().setName("steve").setCountry("canada").build();
        User sally = User.newBuilder().setName("sally").setCountry("united states").build();
        User john = User.newBuilder().setName("john").setCountry("germany").build();
        User fred = User.newBuilder().setName("fred").setCountry("").build();
        User sue = User.newBuilder().setName("sue").build();

        List<User> input = new ArrayList<>();
        input.add(michael);
        input.add(tim);
        input.add(jill);
        input.add(lucas);
        input.add(steve);
        input.add(sally);
        input.add(john);
        input.add(fred);
        input.add(sue);

        List<User> expectedUsa = new ArrayList<>();
        expectedUsa.add(michael);
        expectedUsa.add(sally);

        List<User> expectedGer = new ArrayList<>();
        expectedGer.add(jill);
        expectedGer.add(john);

        List<User> expectedMex = new ArrayList<>();
        expectedMex.add(tim);

        List<User> expectedOther = new ArrayList<>();
        expectedOther.add(lucas);
        expectedOther.add(steve);
        expectedOther.add(fred);
        expectedOther.add(sue);

        for (User user : input) {
            testDriver.pipeInput(inputFactory.create(inputTopic, user.getName(), user));
        }

        List<User> actualUsa = readOutputTopic(testDriver, outputUsaTopic, keyDeserializer, valueDeserializer);
        List<User> actualGer = readOutputTopic(testDriver, outputGerTopic, keyDeserializer, valueDeserializer);
        List<User> actualMex = readOutputTopic(testDriver, outputMexTopic, keyDeserializer, valueDeserializer);
        List<User> actualOther = readOutputTopic(testDriver, outputOtherTopic, keyDeserializer, valueDeserializer);

        Assert.assertEquals(expectedUsa, actualUsa);
        Assert.assertEquals(expectedGer, actualGer);
        Assert.assertEquals(expectedMex, actualMex);
        Assert.assertEquals(expectedOther, actualOther);
    }

}