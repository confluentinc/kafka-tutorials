package io.confluent.developer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;


public class KafkaConsumerApplicationTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  @Rule
  public KafkaContainer kafkaContainer = new KafkaContainer();

  @After
  public void tearDown() {
    kafkaContainer.stop();
  }

  @Test
  public void consumerTest() throws Exception {
    final KafkaConsumerApplication consumerApplication = new KafkaConsumerApplication();
    final Properties envProps = consumerApplication.loadEnvProperties(TEST_CONFIG_FILE);

    envProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());

    final Properties consumerProps = consumerApplication.buildProperties(envProps);
    final Set<String> expectedValues = new TreeSet<>(Arrays.asList("foo", "bar", "baz"));

    createTopics(envProps);
    consumerApplication.runConsumer(consumerProps);

    produceTestRecords(envProps);
    // give the consumer time consume
    Thread.sleep(3000);

    consumerApplication.shutdown();

    Set<String> actualValues = new TreeSet<>(consumerApplication.words());
    assertThat(actualValues, equalTo(expectedValues));
  }



  private void produceTestRecords(final Properties properties) {
    final String topic = properties.getProperty("input.topic.name");
    final Properties producerProperties = new Properties();
    producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());

    try (final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties)) {
      producer.send(producerRecord(topic, "foo"));
      producer.send(producerRecord(topic, "bar"));
      producer.send(producerRecord(topic, "baz"));
      producer.flush();
    }
  }

  private void createTopics(final Properties envProps) {
    final Map<String, Object> config = new HashMap<>();
    config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
    try (final AdminClient client = AdminClient.create(config)) {
      final List<NewTopic> topics = new ArrayList<>();
      topics.add(new NewTopic(
          envProps.getProperty("input.topic.name"),
          Integer.parseInt(envProps.getProperty("input.topic.partitions")),
          Short.parseShort(envProps.getProperty("input.topic.replication.factor"))));

      client.createTopics(topics);
    }
  }

  private ProducerRecord<String, String> producerRecord(final String topic,  final String value) {
      return new ProducerRecord<>(topic, value);
  }
}
