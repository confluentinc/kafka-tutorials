package io.confluent.developer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.confluent.developer.avro.Publication;

public class FilterEventsTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  private KafkaProducer<String, Publication> producer;
  private KafkaConsumer<String, Publication> inputConsumer;
  private KafkaConsumer<String, Publication> outputConsumer;

  @ClassRule
  public static KafkaContainer kafka = new KafkaContainer("5.2.1");
  
  @Rule
  public SchemaRegistryContainer schemaRegistryContainer = new SchemaRegistryContainer("5.2.1").withKafka(kafka);

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

    String kafkaBootstrapUrl = kafka.getBootstrapServers();
    envProps.put("bootstrap.servers", kafkaBootstrapUrl);

    String srUrl = "http://" + schemaRegistryContainer.getTarget();
    envProps.put("schema.registry.url", srUrl);

    fe.createTopics(envProps);

    Properties producerProps = fe.buildProducerProperties(envProps);
    Properties inputConsumerProps = fe.buildConsumerProperties("inputGroup", envProps);
    Properties outputConsumerProps = fe.buildConsumerProperties("outputGroup", envProps);

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    Publication iceAndFire = new Publication("George R. R. Martin", "A Song of Ice and Fire");
    Publication silverChair = new Publication("C.S. Lewis", "The Silver Chair");
    Publication perelandra = new Publication("C.S. Lewis", "Perelandra");
    Publication fireAndBlood = new Publication("George R. R. Martin", "Fire & Blood");
    Publication theHobbit = new Publication("J. R. R. Tolkien", "The Hobbit");
    Publication lotr = new Publication("J. R. R. Tolkien", "The Lord of the Rings");
    Publication dreamOfSpring = new Publication("George R. R. Martin", "A Dream of Spring");
    Publication fellowship = new Publication("J. R. R. Tolkien", "The Fellowship of the Ring");
    Publication iceDragon = new Publication("George R. R. Martin", "The Ice Dragon");

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
    fe.applyFilter(inputTopic, outputTopic, inputConsumer, producer, "George R. R. Martin");

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