package io.confluent.developer;


import io.confluent.developer.avro.Pageviews;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


public class KafkaStreamsPunctuationTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    @Test
    public void punctuationTest() throws IOException {
        final KafkaStreamsPunctuation instance = new KafkaStreamsPunctuation();
        final Properties envProps = instance.loadEnvProperties(TEST_CONFIG_FILE);

        final Properties streamProps = instance.buildStreamsProperties(envProps);
        final String pageviewsInputTopic = envProps.getProperty("input.topic.name");
        final String outputTopicName = envProps.getProperty("output.topic.name");

        final Topology topology = instance.buildTopology(envProps);
        try (final TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps)) {

            final SpecificAvroSerde<Pageviews> exampleAvroSerde = KafkaStreamsPunctuation.<Pageviews>getSpecificAvroSerde(envProps);

            final Serializer<String> keySerializer = Serdes.String().serializer();
            final Serializer<Pageviews> exampleSerializer = exampleAvroSerde.serializer();
            final Deserializer<Long> valueDeserializer = Serdes.Long().deserializer();
            final Deserializer<String> keyDeserializer = Serdes.String().deserializer();

            final TestInputTopic<String, Pageviews>  inputTopic = testDriver.createInputTopic(pageviewsInputTopic, keySerializer, exampleSerializer);
            final TestOutputTopic<String, Long> outputTopic = testDriver.createOutputTopic(outputTopicName, keyDeserializer, valueDeserializer);

            final List<Pageviews> pageviews = new ArrayList<>();
            pageviews.add(Pageviews.newBuilder().setViewtime(5L).setPageid("test-page").setUserid("user-1").build());
            pageviews.add(Pageviews.newBuilder().setViewtime(10L).setPageid("test-page").setUserid("user-1").build());
            pageviews.add(Pageviews.newBuilder().setViewtime(5L).setPageid("test-page").setUserid("user-2").build());
            pageviews.add(Pageviews.newBuilder().setViewtime(25L).setPageid("test-page").setUserid("user-3").build());

            final List<KeyValue<String, Long>> expectedResults = Arrays.asList(KeyValue.pair("user-1", 5L),KeyValue.pair("user-1", 15L),KeyValue.pair("user-3", 25L));

            long timestamp = Instant.now().toEpochMilli();
            for (final Pageviews pageview : pageviews) {
                inputTopic.pipeInput(pageview.getUserid(), pageview, timestamp);
                timestamp = Instant.ofEpochMilli(timestamp).plusSeconds(3).toEpochMilli();
            }

            final List<KeyValue<String, Long>> actualResults = outputTopic.readKeyValuesToList();

            assertEquals(expectedResults, actualResults);

            KeyValueStore<String, Long> store = testDriver.getKeyValueStore("view-count-store");

            testDriver.advanceWallClockTime(Duration.ofSeconds(20));
            
            assertSame(store.get("user-1"), 0L);
            assertSame(store.get("user-2"), 0L);
            assertSame(store.get("user-3"), 0L);
        }
    }
}
