package io.confluent.developer.connect.jdbc.specificavro;

import org.apache.avro.Schema;
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
import java.util.Map;
import java.util.Properties;

import io.confluent.developer.avro.City;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static java.util.Arrays.asList;

public class StreamsIngestTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  private SpecificAvroSerde<City> makeSerializer(Properties envProps)
      throws IOException, RestClientException {

    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final Schema schema = City.SCHEMA$;
    client.register(inputTopic + "-value", schema);
    client.register(outputTopic + "-value", schema);

    SpecificAvroSerde<City> serde = new SpecificAvroSerde<>(client);

    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
    serde.configure(config, false);

    return serde;
  }

  @Test
  public void shouldCreateKeyedStream() throws IOException, RestClientException {
    StreamsIngest si = new StreamsIngest();
    Properties envProps = si.loadEnvProperties(TEST_CONFIG_FILE);
    Properties streamProps = si.buildStreamsProperties(envProps);

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final SpecificAvroSerde<City> citySpecificAvroSerde = makeSerializer(envProps);

    Topology topology = si.buildTopology(envProps, citySpecificAvroSerde);
    TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

    Serializer<String> keySerializer = Serdes.String().serializer();
    Deserializer<String> keyDeserializer = Serdes.String().deserializer();

    ConsumerRecordFactory<String, City>
        inputFactory =
        new ConsumerRecordFactory<>(keySerializer, citySpecificAvroSerde.serializer());

    // Fixture
    City c1 = new City(1L, "Raleigh", "NC");
    City c2 = new City(2L, "Mountain View", "CA");
    City c3 = new City(3L, "Knoxville", "TN");
    City c4 = new City(4L, "Houston", "TX");
    City c5 = new City(5L, "Olympia", "WA");
    City c6 = new City(6L, "Bismarck", "ND");
    // end Fixture

    final List<City>
        input = asList(c1, c2, c3, c4, c5, c6);

    final List<Long> expectedOutput = asList(1L, 2L, 3L, 4L, 5L, 6L);

    for (City city : input) {
      testDriver.pipeInput(inputFactory.create(inputTopic, null, city));
    }

    List<Long> actualOutput = new ArrayList<>();
    while (true) {
      ProducerRecord<String, City>
          record =
          testDriver.readOutput(outputTopic, keyDeserializer, citySpecificAvroSerde.deserializer());

      if (record != null) {
        actualOutput.add(Long.parseLong(record.key()));
      } else {
        break;
      }
    }

    Assert.assertEquals(expectedOutput, actualOutput);
  }

}
