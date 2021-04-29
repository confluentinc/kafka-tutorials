package io.confluent.developer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KeyValue;
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
import java.util.Properties;
import java.util.stream.Collectors;

import io.confluent.developer.avro.TicketSale;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static java.util.Arrays.asList;

public class AggregatingCountTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";
  private TopologyTestDriver testDriver;

  private SpecificAvroSerde<TicketSale> makeSerializer(Properties allProps) {
    SpecificAvroSerde<TicketSale> serde = new SpecificAvroSerde<>();
    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", allProps.getProperty("schema.registry.url"));
    serde.configure(config, false);
    return serde;
  }

  @Test
  public void shouldCountTicketSales() throws IOException {
    AggregatingCount aggCount = new AggregatingCount();
    Properties allProps = aggCount.loadEnvProperties(TEST_CONFIG_FILE);

    String inputTopic = allProps.getProperty("input.topic.name");
    String outputTopic = allProps.getProperty("output.topic.name");

    final SpecificAvroSerde<TicketSale> ticketSaleSpecificAvroSerde = makeSerializer(allProps);

    Topology topology = aggCount.buildTopology(allProps, ticketSaleSpecificAvroSerde);
    testDriver = new TopologyTestDriver(topology, allProps);

    Serializer<String> keySerializer = Serdes.String().serializer();
    Deserializer<String> keyDeserializer = Serdes.String().deserializer();
    
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

    testDriver
        .createInputTopic(inputTopic, keySerializer, ticketSaleSpecificAvroSerde.serializer())
        .pipeValueList(input);

    final String outputLabel = " tickets sold";    
    
    List<String> originalCounts = new ArrayList<String>(Arrays.asList("1", "2", "1", "3", "2", "1", "2", "3", "4"));
    List<String> expectedOutput = originalCounts.stream().map(v -> v + outputLabel).collect(Collectors.toList());

    final List<KeyValue<String, String>> keyValues =
        testDriver
            .createOutputTopic(outputTopic, keyDeserializer, Serdes.String().deserializer())
            .readKeyValuesToList();

    List<String> actualOutput;
    actualOutput = keyValues
        .stream()
        .filter(record -> record.value != null)
        .map(record -> record.value)
        .collect(Collectors.toList());
    
//    System.out.println(actualOutput);
    Assert.assertEquals(expectedOutput, actualOutput);

  }

  @After
  public void cleanup() {
    testDriver.close();
  }
}
