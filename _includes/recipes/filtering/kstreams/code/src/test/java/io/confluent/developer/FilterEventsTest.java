package io.confluent.developer;

import io.confluent.developer.avro.User;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class FilterEventsTest {

    public SpecificAvroSerializer<User> makeSerializer(Properties envProps) {
        SpecificAvroSerializer<User> serializer = new SpecificAvroSerializer<>();
        serializer.configure(new HashMap<String, String>() {{
            put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        }}, false);

        return serializer;
    }

    public SpecificAvroDeserializer<User> makeDeserializer(Properties envProps) {
        SpecificAvroDeserializer<User> deserializer = new SpecificAvroDeserializer<>();
        deserializer.configure(new HashMap<String, String>() {{
            put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        }}, false);

        return deserializer;
    }

    @Test
    public void testFilter() throws IOException {
        FilterEvents fe = new FilterEvents();
        Properties envProps = fe.getEnvProperties();
        Properties streamProps = fe.buildStreamsProperties(envProps);

        String inputTopic = envProps.getProperty("input.topic");
        String outputTopic = envProps.getProperty("output.topic");

        Topology topology = fe.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<Long> keySerializer = Serdes.Long().serializer();
        SpecificAvroSerializer<User> valueSerializer = makeSerializer(envProps);

        Deserializer<Long> keyDeserializer = Serdes.Long().deserializer();
        SpecificAvroDeserializer<User> valueDeserializer = makeDeserializer(envProps);

        ConsumerRecordFactory<Long, User> inputFactory = new ConsumerRecordFactory<>(inputTopic, keySerializer, valueSerializer);

        User michael = User.newBuilder().setName("michael").setFavoriteNumber(42).setFavoriteColor("green").build();
        User tim = User.newBuilder().setName("tim").setFavoriteNumber(8).setFavoriteColor("green").build();
        User jill = User.newBuilder().setName("jill").setFavoriteNumber(500).setFavoriteColor("red").build();
        User lucas = User.newBuilder().setName("lucas").setFavoriteNumber(71).setFavoriteColor("blue").build();
        User steve = User.newBuilder().setName("steve").setFavoriteNumber(23).setFavoriteColor("green").build();
        User sally = User.newBuilder().setName("sally").setFavoriteNumber(63).setFavoriteColor("orange").build();
        User john = User.newBuilder().setName("john").setFavoriteNumber(88).setFavoriteColor("green").build();
        User fred = User.newBuilder().setName("fred").setFavoriteNumber(202).setFavoriteColor("blue").build();
        User sue = User.newBuilder().setName("sue").setFavoriteNumber(0).setFavoriteColor("green").build();

        List<User> input = new ArrayList<User>() {{
            add(michael);
            add(tim);
            add(jill);
            add(lucas);
            add(steve);
            add(sally);
            add(john);
            add(fred);
            add(sue);
        }};

        List<User> expectedOutput = new ArrayList<User>() {{
            add(michael);
            add(tim);
            add(steve);
            add(john);
            add(sue);
        }};

        for (User user : input) {
            testDriver.pipeInput(inputFactory.create(0L, user));
        }

        List<User> actualOutput = new ArrayList<>();
        while (true) {
            ProducerRecord<Long, User> record = testDriver.readOutput(outputTopic, keyDeserializer, valueDeserializer);

            if (record != null) {
                actualOutput.add(record.value());
            } else {
                break;
            }
        }

        Assert.assertEquals(expectedOutput, actualOutput);
    }

}
