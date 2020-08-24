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

  private SpecificAvroSerde<TicketSale> makeSerializer(Properties envProps) {
    SpecificAvroSerde<TicketSale> serde = new SpecificAvroSerde<>();
    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
    serde.configure(config, false);
    return serde;
  }

  @Test
  public void shouldCountTicketSales() throws IOException {
    AggregatingCount aggCount = new AggregatingCount();
    Properties envProps = aggCount.loadEnvProperties(TEST_CONFIG_FILE);
    Properties streamProps = aggCount.buildStreamsProperties(envProps);

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final SpecificAvroSerde<TicketSale> ticketSaleSpecificAvroSerde = makeSerializer(envProps);

    Topology topology = aggCount.buildTopology(envProps, ticketSaleSpecificAvroSerde);
    testDriver = new TopologyTestDriver(topology, streamProps);

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
    
    List<Long> expectedOutput = new ArrayList<Long>(Arrays.asList(1L, 2L, 1L, 3L, 2L, 1L, 2L, 3L, 4L));

    final List<KeyValue<String, Long>> keyValues = 
        testDriver
            .createOutputTopic(outputTopic, keyDeserializer, Serdes.Long().deserializer())
            .readKeyValuesToList();

    List<Long> actualOutput;
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
