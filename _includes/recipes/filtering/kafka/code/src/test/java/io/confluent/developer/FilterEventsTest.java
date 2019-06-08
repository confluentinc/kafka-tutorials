package io.confluent.developer;

import io.confluent.developer.avro.Publication;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class FilterEventsTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    private KafkaProducer<String, Publication> producer;
    private KafkaConsumer<String, Publication> inputConsumer;
    private KafkaConsumer<String, Publication> outputConsumer;

    @After
    public void tearDown() {

        outputConsumer.close();
        inputConsumer.close();
        producer.close();

    }

    @Test
    public void testFilter() throws IOException {

        FilterEvents fe = new FilterEvents();
        Properties envProps = fe.loadEnvProperties(TEST_CONFIG_FILE);
        fe.createTopics(envProps);

        Properties producerProps = fe.buildProducerProperties(envProps);
        Properties inputConsumerProps = fe.buildConsumerProperties("inputGroup", envProps);
        Properties outputConsumerProps = fe.buildConsumerProperties("outputGroup", envProps);

        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

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

        producer = fe.createProducer(producerProps);
        fe.produceRecords(inputTopic, input, producer);

        inputConsumer = fe.createConsumer(inputConsumerProps);
        fe.applyFilter(inputTopic, outputTopic, inputConsumer,
            producer, "George R. R. Martin");

        List<Publication> expectedOutput = new ArrayList<>();
        expectedOutput.add(iceAndFire);
        expectedOutput.add(fireAndBlood);
        expectedOutput.add(dreamOfSpring);
        expectedOutput.add(iceDragon);

        outputConsumer = fe.createConsumer(outputConsumerProps);
        List<Publication> actualOutput = fe.consumeRecords(outputTopic, outputConsumer);

        Assert.assertEquals(expectedOutput, actualOutput);

    }

}