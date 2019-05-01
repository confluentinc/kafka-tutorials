package io.confluent.developer;

import io.confluent.developer.avro.Publication;
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

public class FilterEventsTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    public SpecificAvroSerializer<Publication> makeSerializer(Properties envProps) {
        SpecificAvroSerializer<Publication> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    public SpecificAvroDeserializer<Publication> makeDeserializer(Properties envProps) {
        SpecificAvroDeserializer<Publication> deserializer = new SpecificAvroDeserializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    @Test
    public void testFilter() throws IOException {
        FilterEvents fe = new FilterEvents();
        Properties envProps = fe.loadEnvProperties(TEST_CONFIG_FILE);
        Properties streamProps = fe.buildStreamsProperties(envProps);

        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        Topology topology = fe.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<String> keySerializer = Serdes.String().serializer();
        SpecificAvroSerializer<Publication> valueSerializer = makeSerializer(envProps);

        Deserializer<String> keyDeserializer = Serdes.String().deserializer();
        SpecificAvroDeserializer<Publication> valueDeserializer = makeDeserializer(envProps);

        ConsumerRecordFactory<String, Publication> inputFactory = new ConsumerRecordFactory<>(keySerializer, valueSerializer);

        Publication iceAndFire = Publication.newBuilder().setName("George R. R. Martin").setTitle("A Song of Ice and Fire").build();
        Publication silverChair = Publication.newBuilder().setName("C.S. Lewis").setTitle("The Silver Chair").build();
        Publication perelandra = Publication.newBuilder().setName("C.S. Lewis").setTitle("Perelandra").build();
        Publication fireAndBlood = Publication.newBuilder().setName("George R. R. Martin").setTitle("Fire & Blood").build();
        Publication theHobbit = Publication.newBuilder().setName("J. R. R. Tolkien").setTitle("The Hobbit").build();
        Publication lotr = Publication.newBuilder().setName("J. R. R. Tolkien").setTitle("The Lord of the Rings").build();
        Publication dreamOfSpring = Publication.newBuilder().setName("George R. R. Martin").setTitle("A Dream of Spring").build();
        Publication fellowship = Publication.newBuilder().setName("J. R. R. Tolkien").setTitle("The Fellowship of the Ring").build();
        Publication iceDragon = Publication.newBuilder().setName("George R. R. Martin").setTitle("The Ice Dragon").build();

        List<Publication> input = new ArrayList<>();
        input.add(iceAndFire);
        input.add(silverChair);
        input.add(perelandra);
        input.add(fireAndBlood);
        input.add(theHobbit);
        input.add(lotr);
        input.add(dreamOfSpring);
        input.add(fellowship);
        input.add(iceDragon);

        List<Publication> expectedOutput = new ArrayList<>();
        expectedOutput.add(iceAndFire);
        expectedOutput.add(fireAndBlood);
        expectedOutput.add(dreamOfSpring);
        expectedOutput.add(iceDragon);

        for (Publication publication : input) {
            testDriver.pipeInput(inputFactory.create(inputTopic, publication.getName(), publication));
        }

        List<Publication> actualOutput = new ArrayList<>();
        while (true) {
            ProducerRecord<String, Publication> record = testDriver.readOutput(outputTopic, keyDeserializer, valueDeserializer);

            if (record != null) {
                actualOutput.add(record.value());
            } else {
                break;
            }
        }

        Assert.assertEquals(expectedOutput, actualOutput);
    }

}
