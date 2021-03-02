package io.confluent.developer;


import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThrows;


public class StreamsUncaughtExceptionHandlingTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;
    private TopologyTestDriver testDriver;


    @Before
    public void setUp() throws IOException {
        final StreamsUncaughtExceptionHandling instance = new StreamsUncaughtExceptionHandling();
        final Properties envProps = instance.loadEnvProperties(TEST_CONFIG_FILE);

        final Properties streamProps = instance.buildStreamsProperties(envProps);
        final String sessionDataInputTopic = envProps.getProperty("input.topic.name");
        final String outputTopicName = envProps.getProperty("output.topic.name");

        final Topology topology = instance.buildTopology(envProps);
        testDriver = new TopologyTestDriver(topology, streamProps);
        final Serializer<String> keySerializer = Serdes.String().serializer();
        final Serializer<String> exampleSerializer = Serdes.String().serializer();
        final Deserializer<String> valueDeserializer = Serdes.String().deserializer();
        final Deserializer<String> keyDeserializer = Serdes.String().deserializer();

        inputTopic = testDriver.createInputTopic(sessionDataInputTopic, keySerializer, exampleSerializer);
        outputTopic = testDriver.createOutputTopic(outputTopicName, keyDeserializer, valueDeserializer);
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void shouldThrowException() {
        assertThrows(org.apache.kafka.streams.errors.StreamsException.class, () -> inputTopic.pipeValueList(Arrays.asList("foo", "bar")));
    }

    @Test
    public void shouldProcessValues() {
        List<String> validMessages =  Collections.singletonList("foo");
        List<String> expectedMessages = validMessages.stream().map(String::toUpperCase).collect(Collectors.toList());
        inputTopic.pipeValueList(validMessages);
        List<String> actualResults = outputTopic.readValuesToList();
        assertEquals(expectedMessages, actualResults);
    }

}
