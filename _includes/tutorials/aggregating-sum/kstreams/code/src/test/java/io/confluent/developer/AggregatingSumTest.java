package io.confluent.developer;

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

import io.confluent.developer.avro.TicketSale;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static java.util.Arrays.asList;

public class AggregatingSumTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  private SpecificAvroSerde<TicketSale> makeSerializer(Properties envProps)
      throws IOException, RestClientException {

    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final Schema schema = TicketSale.SCHEMA$;
    client.register(inputTopic + "-value", schema);
    client.register(outputTopic + "-value", schema);

    SpecificAvroSerde<TicketSale> serde = new SpecificAvroSerde<>(client);

    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
    serde.configure(config, false);

    return serde;
  }

  @Test
  public void shouldSumTicketSales() throws IOException, RestClientException {
    AggregatingSum fe = new AggregatingSum();
    Properties envProps = fe.loadEnvProperties(TEST_CONFIG_FILE);
    Properties streamProps = fe.buildStreamsProperties(envProps);

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final SpecificAvroSerde<TicketSale> ticketSaleSpecificAvroSerde = makeSerializer(envProps);

    Topology topology = fe.buildTopology(envProps, ticketSaleSpecificAvroSerde);
    TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

    Serializer<String> keySerializer = Serdes.String().serializer();
    Deserializer<String> keyDeserializer = Serdes.String().deserializer();

    ConsumerRecordFactory<String, TicketSale>
        inputFactory =
        new ConsumerRecordFactory<>(keySerializer, ticketSaleSpecificAvroSerde.serializer());

    final List<TicketSale>
        input = asList(
                  new TicketSale("Die Hard", "2019-07-18T10:00:00Z", 12),
                  new TicketSale("Die Hard", "2019-07-18T10:01:00Z", 12),
                  new TicketSale("The Godfather", "2019-07-18T10:01:31Z", 12),
                  new TicketSale("Die Hard", "2019-07-18T10:01:36Z", 24),
                  new TicketSale("The Godfather", "2019-07-18T10:02:00Z", 18),
                  new TicketSale("The Big Lebowski", "2019-07-18T11:03:21Z", 12),
                  new TicketSale("The Big Lebowski", "2019-07-18T11:03:50Z", 12),
                  new TicketSale("The Godfather", "2019-07-18T11:40:00Z", 36),
                  new TicketSale("The Godfather", "2019-07-18T11:40:09Z", 18)
                );

    for (TicketSale ticketSale : input) {
      testDriver.pipeInput(inputFactory.create(inputTopic, "", ticketSale));
    }

    List<TicketSale> actualOutput = new ArrayList<>();
    while (true) {
      ProducerRecord<String, TicketSale>
          record =
          testDriver.readOutput(outputTopic, keyDeserializer, ticketSaleSpecificAvroSerde.deserializer());

      if (record != null) {
        actualOutput.add(record.value());
      } else {
        break;
      }
    }

    //Assert.assertEquals(expectedOutput, actualOutput);
  }

}
