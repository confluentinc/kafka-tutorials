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

import io.confluent.developer.avro.Publication;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static java.util.Arrays.asList;

public class FilterEventsTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  private SpecificAvroSerde<Publication> makeSerializer(Properties envProps)
      throws IOException, RestClientException {

    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final Schema schema = Publication.SCHEMA$;
    client.register(inputTopic + "-value", schema);
    client.register(outputTopic + "-value", schema);

    SpecificAvroSerde<Publication> serde = new SpecificAvroSerde<>(client);

    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
    serde.configure(config, false);

    return serde;
  }

  @Test
  public void shouldFilterGRRMartinsBooks() throws IOException, RestClientException {
    FilterEvents fe = new FilterEvents();
    Properties envProps = fe.loadEnvProperties(TEST_CONFIG_FILE);
    Properties streamProps = fe.buildStreamsProperties(envProps);

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final SpecificAvroSerde<Publication> publicationSpecificAvroSerde = makeSerializer(envProps);

    Topology topology = fe.buildTopology(envProps, publicationSpecificAvroSerde);
    TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

    Serializer<String> keySerializer = Serdes.String().serializer();
    Deserializer<String> keyDeserializer = Serdes.String().deserializer();

    ConsumerRecordFactory<String, Publication>
        inputFactory =
        new ConsumerRecordFactory<>(keySerializer, publicationSpecificAvroSerde.serializer());

    // Fixture
    Publication iceAndFire = new Publication("George R. R. Martin", "A Song of Ice and Fire");
    Publication silverChair = new Publication("C.S. Lewis", "The Silver Chair");
    Publication perelandra = new Publication("C.S. Lewis", "Perelandra");
    Publication fireAndBlood = new Publication("George R. R. Martin", "Fire & Blood");
    Publication theHobbit = new Publication("J. R. R. Tolkien", "The Hobbit");
    Publication lotr = new Publication("J. R. R. Tolkien", "The Lord of the Rings");
    Publication dreamOfSpring = new Publication("George R. R. Martin", "A Dream of Spring");
    Publication fellowship = new Publication("J. R. R. Tolkien", "The Fellowship of the Ring");
    Publication iceDragon = new Publication("George R. R. Martin", "The Ice Dragon");
    // end Fixture

    final List<Publication>
        input = asList(iceAndFire, silverChair, perelandra, fireAndBlood, theHobbit, lotr, dreamOfSpring, fellowship,
                       iceDragon);

    final List<Publication> expectedOutput = asList(iceAndFire, fireAndBlood, dreamOfSpring, iceDragon);

    for (Publication publication : input) {
      testDriver.pipeInput(inputFactory.create(inputTopic, publication.getName(), publication));
    }

    List<Publication> actualOutput = new ArrayList<>();
    while (true) {
      ProducerRecord<String, Publication>
          record =
          testDriver.readOutput(outputTopic, keyDeserializer, publicationSpecificAvroSerde.deserializer());

      if (record != null) {
        actualOutput.add(record.value());
      } else {
        break;
      }
    }

    Assert.assertEquals(expectedOutput, actualOutput);
  }

}
