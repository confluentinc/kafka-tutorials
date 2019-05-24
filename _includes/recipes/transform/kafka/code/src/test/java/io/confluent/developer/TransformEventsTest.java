package io.confluent.developer;

import io.confluent.developer.avro.Publication;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class TransformEventsTest {

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
    public void testTransformation() throws IOException {

        TransformEvents te = new TransformEvents();
        Properties envProps = te.loadEnvProperties(TEST_CONFIG_FILE);
        te.createTopics(envProps);

        Properties producerProps = te.buildProducerProperties(envProps);
        Properties inputConsumerProps = te.buildConsumerProperties("inputGroup", envProps);
        Properties outputConsumerProps = te.buildConsumerProperties("outputGroup", envProps);

        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        Publication iceAndFire = Publication.newBuilder().setName("George R. R. Martin").setTitle("A Song of Ice and Fire").build();
        Publication silverChair = Publication.newBuilder().setName("C.S. Lewis").setTitle("The Silver Chair").build();

        List<Publication> input = new ArrayList<>();
        input.add(iceAndFire);
        input.add(silverChair);

        producer = te.createProducer(producerProps);
        te.produceRecords(inputTopic, input, producer);

        inputConsumer = te.createConsumer(inputConsumerProps);
        te.applyTransformation(inputTopic, outputTopic,
            inputConsumer, producer);

        Publication iceAndFireUpper = Publication.newBuilder().setName("GEORGE R. R. MARTIN").setTitle("A SONG OF ICE AND FIRE").build();
        Publication silverChairUpper = Publication.newBuilder().setName("C.S. LEWIS").setTitle("THE SILVER CHAIR").build();

        List<Publication> expectedOutput = new ArrayList<>();
        expectedOutput.add(iceAndFireUpper);
        expectedOutput.add(silverChairUpper);

        outputConsumer = te.createConsumer(outputConsumerProps);
        List<Publication> actualOutput = te.consumeRecords(outputTopic, outputConsumer);

        Assert.assertEquals(expectedOutput, actualOutput);

    }

}