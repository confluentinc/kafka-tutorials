package io.confluent.developer.serialization;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import io.confluent.developer.avro.Movie;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class SerializationTutorialTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  private SpecificAvroSerde<Movie> makeAvroSerDe(Properties envProps) throws IOException, RestClientException {

    // MockSchemaRegistryClient doesn't require connection to Schema Registry which is perfect for unit test
    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    String outputTopic = envProps.getProperty("output.avro.movies.topic.name");
    client.register(outputTopic + "-value", Movie.SCHEMA$);
    final SpecificAvroSerde<Movie> movieAvroSerde = new SpecificAvroSerde<>(client);

    final HashMap<String, String> map = new HashMap<>();

    // this should be unnecessary because we use MockSchemaRegistryClient
    // but still required in order to avoid `io.confluent.common.config.ConfigException: Missing required configuration "schema.registry.url" which has no default value.`
    map.put("schema.registry.url", envProps.getProperty("schema.registry.url"));

    movieAvroSerde.configure(map, false);
    return movieAvroSerde;
  }

  @Test
  public void shouldChangeSerializationFormat() throws IOException, RestClientException {
    SerializationTutorial sr = new SerializationTutorial();
    final Properties envProps = sr.loadEnvProperties(TEST_CONFIG_FILE);
    final Properties streamsProps = sr.buildStreamsProperties(envProps);

    String inputTopic = envProps.getProperty("input.json.movies.topic.name");
    String outputTopic = envProps.getProperty("output.avro.movies.topic.name");

    final SpecificAvroSerde<Movie> avroSerde = this.makeAvroSerDe(envProps);
    Topology topology = sr.buildTopology(envProps, avroSerde);
    TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamsProps);

    final Serializer<Long> keySerializer = new LongSerializer();
    // Json serializer
    final Serializer<byte[]> valueSerializer = Serdes.ByteArray().serializer();

    final Deserializer<Long> keyDeserializer = new LongDeserializer();
    // Avro deserializer
    final SpecificAvroSerde<Movie> movieSpecificAvroSerde = makeAvroSerDe(envProps);

    final ConsumerRecordFactory<Long, byte[]>
        inputFactory =
        new ConsumerRecordFactory<>(keySerializer, valueSerializer);

    final List<JsonObject> input = prepareInputFixture();
    final List<Movie> expectedMovies = prepareExpectedFixture();

    for (JsonObject json : input) {
      testDriver.pipeInput(inputFactory.create(inputTopic,
                                               json.getAsJsonPrimitive("movie_id").getAsLong(),
                                               json.toString().getBytes(Charset.forName("UTF-8"))));
    }

    for (int i = 0; i < 3; i++) {
      final ProducerRecord<Long, Movie>
          actual =
          testDriver.readOutput(outputTopic, keyDeserializer, movieSpecificAvroSerde.deserializer());
      OutputVerifier.compareKeyValue(actual, new ProducerRecord<>(outputTopic, (long) i + 1, expectedMovies.get(i)));
    }
  }

  /**
   * Prepares expected movies in avro format
   *
   * @return a list of three (3) movie
   */
  private List<Movie> prepareExpectedFixture() {
    final Movie lethalWeaponExpected = new Movie(1L, "Lethal Weapon", 1992);
    final Movie dieHardExpected = new Movie(2L, "Die Hard", 1988);
    final Movie predatorExpected = new Movie(3L, "Predator", 1987);

    return Arrays.asList(lethalWeaponExpected, dieHardExpected, predatorExpected);
  }

  /**
   * Prepares test data in JSON format
   *
   * @return a list of three (3) movies
   */
  private List<JsonObject> prepareInputFixture() {
    final JsonObject lethalWeaponJson = new JsonObject();
    lethalWeaponJson.add("movie_id", new JsonPrimitive(1));
    lethalWeaponJson.add("title", new JsonPrimitive("Lethal Weapon"));
    lethalWeaponJson.add("release_year", new JsonPrimitive(1992));

    final JsonObject dieHardJson = new JsonObject();
    dieHardJson.add("movie_id", new JsonPrimitive(2));
    dieHardJson.add("title", new JsonPrimitive("Die Hard"));
    dieHardJson.add("release_year", new JsonPrimitive(1988));

    final JsonObject predatorJson = new JsonObject();
    predatorJson.add("movie_id", new JsonPrimitive(3));
    predatorJson.add("title", new JsonPrimitive("Predator"));
    predatorJson.add("release_year", new JsonPrimitive(1987));

    return Arrays.asList(lethalWeaponJson, dieHardJson, predatorJson);
  }
}
