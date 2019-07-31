package io.confluent.developer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RatedMovie;
import io.confluent.developer.avro.Rating;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.lang.System.out;

public class JoinStreamToTable {

  public Properties buildStreamsProperties(Properties envProps) throws IOException {
    Properties props = new Properties();

    props.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("application.id"));
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));

    final String tempDirectory = Files.createTempDirectory("kafka-streams")
        .toAbsolutePath()
        .toString();
    props.setProperty(StreamsConfig.STATE_DIR_CONFIG, tempDirectory);

    return props;
  }

  public Topology buildTopology(Properties envProps,
                                SpecificAvroSerde<Movie> movieSerde,
                                SpecificAvroSerde<Rating> ratingSerde,
                                SpecificAvroSerde<RatedMovie> ratedMovieSerde) {

    final StreamsBuilder builder = new StreamsBuilder();
    final String movieTopic = envProps.getProperty("movie.topic.name");
    final String rekeyedMovieTopic = envProps.getProperty("rekeyed.movie.topic.name");
    final String ratingTopic = envProps.getProperty("rating.topic.name");
    final String ratedMoviesTopic = envProps.getProperty("rated.movies.topic.name");
    final MovieRatingJoiner joiner = new MovieRatingJoiner();

    final Serde<String> stringKeySerde = Serdes.String();

    KStream<String, Movie> movieStream = builder
        .stream(movieTopic, Consumed.with(stringKeySerde, movieSerde))
        .map((key, movie) -> new KeyValue<>(movie.getId().toString(), movie));

    movieStream.to(rekeyedMovieTopic, Produced.with(stringKeySerde, movieSerde));

    KTable<String, Movie> movies = builder
        .table(rekeyedMovieTopic,
               Materialized.<String, Movie, KeyValueStore<Bytes, byte[]>>as(rekeyedMovieTopic + "-store")
                   .withKeySerde(Serdes.String()).withValueSerde(movieSerde));

    KStream<String, Rating> ratings = builder
        .stream(ratingTopic, Consumed.with(Serdes.String(), ratingSerde))
        .peek((k, v) -> out.println(k + " " + v))
        .selectKey((key, value) -> value.getId().toString());
        //.map((key, rating) -> new KeyValue<>(rating.getId().toString(), rating));

    KStream<String, RatedMovie> ratedMovie = ratings.join(movies, joiner);

    ratedMovie.to(ratedMoviesTopic, Produced.with(Serdes.String(), ratedMovieSerde));

    return builder.build();
  }

  private SpecificAvroSerde<RatedMovie> ratedMovieAvroSerde(Properties envProps) {
    SpecificAvroSerde<RatedMovie> movieAvroSerde = new SpecificAvroSerde<>();

    final HashMap<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG,
                    envProps.getProperty("schema.registry.url"));

    movieAvroSerde.configure(serdeConfig, false);
    return movieAvroSerde;
  }

  private SpecificAvroSerde<Movie> movieAvroSerde(Properties envProps) {
    SpecificAvroSerde<Movie> movieAvroSerde = new SpecificAvroSerde<>();

    final HashMap<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG,
                    envProps.getProperty("schema.registry.url"));

    movieAvroSerde.configure(serdeConfig, false);
    return movieAvroSerde;
  }

  private SpecificAvroSerde<Rating> ratingAvroSerde(final Properties envProps) {
    SpecificAvroSerde<Rating> ratingSerde = new SpecificAvroSerde<>();

    final HashMap<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG,
                    envProps.getProperty("schema.registry.url"));

    ratingSerde.configure(serdeConfig, false);
    return ratingSerde;
  }

  public void createTopics(Properties envProps) {
    Map<String, Object> config = new HashMap<>();
    config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
    try (AdminClient client = AdminClient.create(config)) {

      List<NewTopic> topics = new ArrayList<>();

      topics.add(new NewTopic(
          envProps.getProperty("movie.topic.name"),
          Integer.parseInt(envProps.getProperty("movie.topic.partitions")),
          Short.parseShort(envProps.getProperty("movie.topic.replication.factor"))));

      topics.add(new NewTopic(
          envProps.getProperty("rekeyed.movie.topic.name"),
          Integer.parseInt(envProps.getProperty("rekeyed.movie.topic.partitions")),
          Short.parseShort(envProps.getProperty("rekeyed.movie.topic.replication.factor"))));

      topics.add(new NewTopic(
          envProps.getProperty("rating.topic.name"),
          Integer.parseInt(envProps.getProperty("rating.topic.partitions")),
          Short.parseShort(envProps.getProperty("rating.topic.replication.factor"))));

      topics.add(new NewTopic(
          envProps.getProperty("rated.movies.topic.name"),
          Integer.parseInt(envProps.getProperty("rated.movies.topic.partitions")),
          Short.parseShort(envProps.getProperty("rated.movies.topic.replication.factor"))));

      client.createTopics(topics);
    }
  }

  public Properties loadEnvProperties(String fileName) throws IOException {
    Properties envProps = new Properties();
    FileInputStream input = new FileInputStream(fileName);
    envProps.load(input);
    input.close();

    return envProps;
  }

  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      throw new IllegalArgumentException(
          "This program takes one argument: the path to an environment configuration file.");
    }

    new JoinStreamToTable().runRecipe(args[0]);
  }

  private void runRecipe(final String configPath) throws IOException {
    JoinStreamToTable ts = new JoinStreamToTable();
    Properties envProps = ts.loadEnvProperties(configPath);
    Properties streamProps = ts.buildStreamsProperties(envProps);
    Topology topology = ts.buildTopology(envProps,
                                         movieAvroSerde(envProps),
                                         ratingAvroSerde(envProps),
                                         ratedMovieAvroSerde(envProps));
    ts.createTopics(envProps);

    final KafkaStreams streams = new KafkaStreams(topology, streamProps);
    final CountDownLatch latch = new CountDownLatch(1);

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }
}
