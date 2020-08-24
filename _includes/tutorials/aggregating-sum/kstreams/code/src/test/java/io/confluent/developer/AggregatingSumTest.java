package io.confluent.developer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import io.confluent.developer.avro.TicketSale;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static java.util.Arrays.asList;

public class AggregatingSumTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";
  private TopologyTestDriver testDriver;

  private SpecificAvroSerde<TicketSale> makeSerializer(Properties envProps) {
    SpecificAvroSerde<TicketSale> serde = new SpecificAvroSerde<>();

    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
    serde.configure(config, false);

    return serde;
  }

  @Test
  public void shouldSumTicketSales() throws IOException {
    AggregatingSum aggSum = new AggregatingSum();
    Properties envProps = aggSum.loadEnvProperties(TEST_CONFIG_FILE);
    Properties streamProps = aggSum.buildStreamsProperties(envProps);

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final SpecificAvroSerde<TicketSale> ticketSaleSpecificAvroSerde = makeSerializer(envProps);

    Topology topology = aggSum.buildTopology(envProps, ticketSaleSpecificAvroSerde);
    testDriver = new TopologyTestDriver(topology, streamProps);

    Serializer<String> keySerializer = Serdes.String().serializer();
    Deserializer<String> keyDeserializer = Serdes.String().deserializer();

    final TestInputTopic<String, TicketSale>
        testDriverInputTopic =
        testDriver.createInputTopic(inputTopic, keySerializer, ticketSaleSpecificAvroSerde.serializer());
    
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

    List<Integer> expectedOutput = new ArrayList<>(Arrays.asList(12, 24, 12, 48, 30, 12, 24, 66, 84));

    for (TicketSale ticketSale : input) {
      testDriverInputTopic.pipeInput("", ticketSale);
    }

    List<Integer> actualOutput =
        testDriver
            .createOutputTopic(outputTopic, keyDeserializer, Serdes.Integer().deserializer())
            .readKeyValuesToList()
            .stream()
            .filter(Objects::nonNull)
            .map(record -> record.value)
            .collect(Collectors.toList());
    
    System.out.println(actualOutput);
    Assert.assertEquals(expectedOutput, actualOutput);

  }

  @After
  public void cleanup() {
    testDriver.close();
  }

}
