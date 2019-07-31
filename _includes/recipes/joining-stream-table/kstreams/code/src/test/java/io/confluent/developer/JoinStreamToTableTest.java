package io.confluent.developer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RatedMovie;
import io.confluent.developer.avro.Rating;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static org.junit.Assert.assertEquals;

public class JoinStreamToTableTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  private Map<String, String> srConfigMap(final Properties envProps) {
    Map<String, String> config = new HashMap<>();
    config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
    return config;
  }

  private SpecificAvroSerde<Movie> makeMovieSerde(Properties envProps)
      throws IOException, RestClientException {

    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    client.register(envProps.getProperty("movie.topic.name") + "-value", Movie.SCHEMA$);

    SpecificAvroSerde<Movie> serde = new SpecificAvroSerde<>(client);
    serde.configure(srConfigMap(envProps), false);
    return serde;
  }

  private SpecificAvroSerde<Rating> makeRatingSerde(Properties envProps)
      throws IOException, RestClientException {

    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    client.register(envProps.getProperty("rating.topic.name") + "-value", Rating.SCHEMA$);

    // constructor with SR client as a parameter of SpecificAvroSerializer is private
    // we need to use SpecificAvroSerde(mockSRClient) to get serializer
    // https://github.com/confluentinc/schema-registry/issues/1086
    SpecificAvroSerde<Rating> serde = new SpecificAvroSerde<>(client);
    serde.configure(srConfigMap(envProps), false);

    return serde;
  }

  private SpecificAvroSerde<RatedMovie> makeRatedMovieSerde(Properties envProps)
      throws IOException, RestClientException {

    final MockSchemaRegistryClient client = new MockSchemaRegistryClient();
    client.register(envProps.getProperty("rated.movies.topic.name"), RatedMovie.SCHEMA$);

    SpecificAvroSerde<RatedMovie> serde = new SpecificAvroSerde<>(client);
    serde.configure(srConfigMap(envProps), false);

    return serde;
  }

  @Test
  public void testJoin() throws IOException, RestClientException {
    JoinStreamToTable jst = new JoinStreamToTable();
    Properties envProps = jst.loadEnvProperties(TEST_CONFIG_FILE);
    Properties streamProps = jst.buildStreamsProperties(envProps);

    String tableTopic = envProps.getProperty("movie.topic.name");
    String streamTopic = envProps.getProperty("rating.topic.name");
    String outputTopic = envProps.getProperty("rated.movies.topic.name");

    final SpecificAvroSerde<Movie> movieSerde = makeMovieSerde(envProps);
    final SpecificAvroSerde<Rating> ratingAvroSerde = makeRatingSerde(envProps);
    final SpecificAvroSerde<RatedMovie> ratedMovieSerde = makeRatedMovieSerde(envProps);

    Serializer<String> keySerializer = Serdes.String().serializer();
    Deserializer<String> keyDeserializer = Serdes.String().deserializer();

    Topology topology = jst.buildTopology(envProps, movieSerde, ratingAvroSerde, ratedMovieSerde);
    TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

    ConsumerRecordFactory<String, Movie> movieFactory =
        new ConsumerRecordFactory<>(keySerializer, movieSerde.serializer());

    ConsumerRecordFactory<String, Rating> ratingFactory =
        new ConsumerRecordFactory<>(keySerializer, ratingAvroSerde.serializer());

    List<Movie> movies = new ArrayList<>();
    movies.add(Movie.newBuilder().setId(294).setTitle("Die Hard").setReleaseYear(1988).build());
    movies.add(Movie.newBuilder().setId(354).setTitle("Tree of Life").setReleaseYear(2011).build());
    movies.add(Movie.newBuilder().setId(782).setTitle("A Walk in the Clouds").setReleaseYear(1998).build());
    movies.add(Movie.newBuilder().setId(128).setTitle("The Big Lebowski").setReleaseYear(1998).build());
    movies.add(Movie.newBuilder().setId(780).setTitle("Super Mario Bros.").setReleaseYear(1993).build());

    List<Rating> ratings = new ArrayList<>();
    ratings.add(new Rating(294L, 8.2));
    ratings.add(new Rating(294L, 8.5));
    ratings.add(new Rating(354L, 9.9));
    ratings.add(new Rating(354L, 9.7));
    ratings.add(new Rating(782L, 7.8));
    ratings.add(new Rating(782L, 7.7));
    ratings.add(new Rating(128L, 8.7));
    ratings.add(new Rating(128L, 8.4));
    ratings.add(new Rating(780L, 2.1));

    List<RatedMovie> ratedMovies = new ArrayList<>();
    ratedMovies.add(RatedMovie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setRating(8.2).build());
    ratedMovies.add(RatedMovie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setRating(8.5).build());
    ratedMovies.add(RatedMovie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setRating(9.9).build());
    ratedMovies.add(RatedMovie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setRating(9.7).build());
    ratedMovies.add(RatedMovie.newBuilder().setId(782).setTitle("A Walk in the Clouds").setReleaseYear(1998).setRating(7.8).build());
    ratedMovies.add(RatedMovie.newBuilder().setId(782).setTitle("A Walk in the Clouds").setReleaseYear(1998).setRating(7.7).build());
    ratedMovies.add(RatedMovie.newBuilder().setId(128).setTitle("The Big Lebowski").setReleaseYear(1998).setRating(8.7).build());
    ratedMovies.add(RatedMovie.newBuilder().setId(128).setTitle("The Big Lebowski").setReleaseYear(1998).setRating(8.4).build());
    ratedMovies.add(RatedMovie.newBuilder().setId(780).setTitle("Super Mario Bros.").setReleaseYear(1993).setRating(2.1).build());

    for (Movie movie : movies) {
      testDriver.pipeInput(movieFactory.create(tableTopic, movie.getId().toString(), movie));
    }

    for (Rating rating : ratings) {
      testDriver.pipeInput(ratingFactory.create(streamTopic, rating.getId().toString(), rating));
    }

    List<RatedMovie> actualOutput = readOutputTopic(testDriver, outputTopic, keyDeserializer, ratedMovieSerde);

    assertEquals(ratedMovies, actualOutput);
  }

  private List<RatedMovie> readOutputTopic(TopologyTestDriver testDriver,
                                           String topic,
                                           Deserializer<String> keyDeserializer,
                                           SpecificAvroSerde<RatedMovie> ratedMovieSpecificAvroSerde) {
    List<RatedMovie> results = new ArrayList<>();

    while (true) {
      ProducerRecord<String, RatedMovie>
          record =
          testDriver.readOutput(topic, keyDeserializer, ratedMovieSpecificAvroSerde.deserializer());

      if (record != null) {
        results.add(record.value());
      } else {
        break;
      }
    }

    return results;
  }

}
