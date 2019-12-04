package io.confluent.developer;

import io.confluent.developer.avro.Click;
import org.apache.avro.Schema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;

public class FindDistinctEventsTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";
  private final static Path STATE_DIR =
          Paths.get(System.getProperty("user.dir"), "build");

  private Properties envProps;
  private Properties streamProps;

  public FindDistinctEventsTest() throws IOException {
    envProps = FindDistinctEvents.loadEnvProperties(TEST_CONFIG_FILE);
    streamProps = FindDistinctEvents.buildStreamsProperties(envProps);
    streamProps.put(StreamsConfig.STATE_DIR_CONFIG, STATE_DIR.toString());
  }

  private static SpecificAvroSerde<Click> makeSerializer(Properties envProps)
      throws IOException, RestClientException {

    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final Schema schema = Click.SCHEMA$;
    client.register(inputTopic + "-value", schema);
    client.register(outputTopic + "-value", schema);

    SpecificAvroSerde<Click> serde = new SpecificAvroSerde<>(client);

    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
    serde.configure(config, false);

    return serde;
  }
  void deleteDirectory(Path path) throws IOException {
    Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
  }
  @Before
  public void before() {
    try {
      deleteDirectory(Paths.get(
              STATE_DIR.toString(),
              envProps.getProperty("application.id")));
    } catch (IOException e) {
    }
  }

  @Test
  public void shouldFilterDistinctEvents() throws IOException, RestClientException {

    final FindDistinctEvents distinctifier  = new FindDistinctEvents();

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final SpecificAvroSerde<Click> clickSerde = makeSerializer(envProps);

    Topology topology = distinctifier.buildTopology(envProps, clickSerde);
    TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

    Serializer<String> keySerializer = Serdes.String().serializer();

    ConsumerRecordFactory<String, Click> inputFactory = new ConsumerRecordFactory<>(
            keySerializer, clickSerde.serializer());

    final List<Click> clicks = asList(
            new Click("10.0.0.1",
                    "https://docs.confluent.io/current/tutorials/examples/kubernetes/gke-base/docs/index.html",
            "2019-09-16T14:53:43+00:00"),
            new Click("10.0.0.2",
                    "https://www.confluent.io/hub/confluentinc/kafka-connect-datagen",
            "2019-09-16T14:53:43+00:01"),
            new Click("10.0.0.3",
            "https://www.confluent.io/hub/confluentinc/kafka-connect-datagen",
            "2019-09-16T14:53:43+00:03"),
            new Click("10.0.0.1",
            "https://docs.confluent.io/current/tutorials/examples/kubernetes/gke-base/docs/index.html",
            "2019-09-16T14:53:43+00:00"),
            new Click("10.0.0.2",
            "https://www.confluent.io/hub/confluentinc/kafka-connect-datagen",
            "2019-09-16T14:53:43+00:01"),
            new Click("10.0.0.3",
            "https://www.confluent.io/hub/confluentinc/kafka-connect-datagen",
            "2019-09-16T14:53:43+00:03"));

    final List<Click> expectedOutput = asList(clicks.get(0),clicks.get(1),clicks.get(2));

    for (Click clk : clicks) {
      testDriver.pipeInput(inputFactory.create(inputTopic, clk.getIp(), clk));
    }

    Deserializer<String> keyDeserializer = Serdes.String().deserializer();
    List<Click> actualOutput = new ArrayList<>();
    while (true) {
      ProducerRecord<String, Click>
          record =
          testDriver.readOutput(outputTopic, keyDeserializer, clickSerde.deserializer());

      if (record != null) {
        actualOutput.add(record.value());
      } else {
        break;
      }
    }

    Assert.assertEquals(expectedOutput, actualOutput);
  }
}
