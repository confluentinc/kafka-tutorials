package io.confluent.developer;

import static org.junit.Assert.assertEquals;

import io.confluent.developer.avro.Example;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Test;


public class StreamsToTableTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    @Test
    public void exampleTest() throws IOException {
        final StreamsToTable instance = new StreamsToTable();
        final Properties envProps = instance.loadEnvProperties(TEST_CONFIG_FILE);

        final Properties streamProps = instance.buildStreamsProperties(envProps);

        final String exampleInputTopic = envProps.getProperty("input.topic.name");
        final String exampleOutputTopic = envProps.getProperty("output.topic.name");
      
        final Topology topology = instance.buildTopology(envProps);
        try (final TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps)) {

            final Serde<Long> longAvroSerde = StreamsToTable.<Long>getPrimitiveAvroSerde(envProps, true);
            final SpecificAvroSerde<Example> exampleAvroSerde = StreamsToTable.<Example>getSpecificAvroSerde(envProps);

            final Serializer<Long> keySerializer = longAvroSerde.serializer();
            final Deserializer<Long> keyDeserializer = longAvroSerde.deserializer();
            final Serializer<Example> exampleSerializer = exampleAvroSerde.serializer();
            final Deserializer<Example> exampleDeserializer = exampleAvroSerde.deserializer();

            final TestInputTopic<Long, Example>  inputTopic = testDriver.createInputTopic(exampleInputTopic, keySerializer, exampleSerializer);
            final TestOutputTopic<Long, Example> outputTopic = testDriver.createOutputTopic(exampleOutputTopic, keyDeserializer, exampleDeserializer);


            final List<Example> examples = new ArrayList<>();
            examples.add(Example.newBuilder().setId(5L).setName("foo").build());
            examples.add(Example.newBuilder().setId(6L).setName("bar").build());
            examples.add(Example.newBuilder().setId(7L).setName("baz").build());

             final List<Example> expectedExamples = new ArrayList<>();
            expectedExamples.add(Example.newBuilder().setId(5L).setName("Hello foo").build());
            expectedExamples.add(Example.newBuilder().setId(6L).setName("Hello bar").build());
            expectedExamples.add(Example.newBuilder().setId(7L).setName("Hello baz").build());


            for (final Example example : examples) {
                inputTopic.pipeInput(example.getId(), example);
            }

            final List<Example> actualExampleResults = outputTopic.readValuesToList();

            assertEquals(expectedExamples, actualExampleResults);
        }
    }
}
