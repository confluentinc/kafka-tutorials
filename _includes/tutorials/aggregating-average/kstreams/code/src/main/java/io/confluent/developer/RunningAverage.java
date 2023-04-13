package io.confluent.developer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import io.confluent.demo.CountAndSum;
import io.confluent.demo.Rating;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.lang.Integer.parseInt;
import static java.lang.Short.parseShort;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.common.serialization.Serdes.Double;
import static org.apache.kafka.common.serialization.Serdes.Long;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.REPLICATION_FACTOR_CONFIG;
import static org.apache.kafka.streams.kstream.Grouped.with;

public class RunningAverage {

  //region buildStreamsProperties
  protected Properties buildStreamsProperties(Properties envProps) {
    Properties config = new Properties();
    config.putAll(envProps);

    config.put(APPLICATION_ID_CONFIG, envProps.getProperty("application.id"));
    config.put(BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
    config.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Long().getClass());
    config.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Double().getClass());
    config.put(SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));

    config.put(REPLICATION_FACTOR_CONFIG, envProps.getProperty("default.topic.replication.factor"));
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, envProps.getProperty("offset.reset.policy"));

    config.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);

    return config;
  }
  //endregion

  //region createTopics

  /**
   * Create topics using AdminClient API
   */
  private void createTopics(Properties envProps) {
    Map<String, Object> config = new HashMap<>();

    config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
    AdminClient client = AdminClient.create(config);

    List<NewTopic> topics = new ArrayList<>();

    topics.add(new NewTopic(
        envProps.getProperty("input.ratings.topic.name"),
        parseInt(envProps.getProperty("input.ratings.topic.partitions")),
        parseShort(envProps.getProperty("input.ratings.topic.replication.factor"))));

    topics.add(new NewTopic(
        envProps.getProperty("output.rating-averages.topic.name"),
        parseInt(envProps.getProperty("output.rating-averages.topic.partitions")),
        parseShort(envProps.getProperty("output.rating-averages.topic.replication.factor"))));

    client.createTopics(topics);
    client.close();

  }
  //endregion

  private void run() {

    Properties envProps = this.loadEnvProperties();
    Properties streamProps = this.buildStreamsProperties(envProps);
    Topology topology = this.buildTopology(new StreamsBuilder(), envProps);

    this.createTopics(envProps);

    final KafkaStreams streams = new KafkaStreams(topology, streamProps);
    final CountDownLatch latch = new CountDownLatch(1);

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close(Duration.ofSeconds(5));
        latch.countDown();
      }
    });

    try {
      streams.cleanUp();
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }

  protected static KTable<Long, Double> getRatingAverageTable(KStream<Long, Rating> ratings,
                                                              String avgRatingsTopicName,
                                                              SpecificAvroSerde<CountAndSum> countAndSumSerde) {

    // Grouping Ratings
    KGroupedStream<Long, Double> ratingsById = ratings
        .map((key, rating) -> new KeyValue<>(rating.getMovieId(), rating.getRating()))
        .groupByKey(with(Long(), Double()));

    final KTable<Long, CountAndSum> ratingCountAndSum =
        ratingsById.aggregate(() -> new CountAndSum(0L, 0.0),
                              (key, value, aggregate) -> {
                                aggregate.setCount(aggregate.getCount() + 1);
                                aggregate.setSum(aggregate.getSum() + value);
                                return aggregate;
                              },
                              Materialized.with(Long(), countAndSumSerde));

    final KTable<Long, Double> ratingAverage =
        ratingCountAndSum.mapValues(value -> value.getSum() / value.getCount(),
                                    Materialized.as("average-ratings"));

    // persist the result in topic
    ratingAverage.toStream().to(avgRatingsTopicName);
    return ratingAverage;
  }

  //region buildTopology
  private Topology buildTopology(StreamsBuilder bldr,
                                 Properties envProps) {

    final String ratingTopicName = envProps.getProperty("input.ratings.topic.name");
    final String avgRatingsTopicName = envProps.getProperty("output.rating-averages.topic.name");

    KStream<Long, Rating> ratingStream = bldr.stream(ratingTopicName,
                                                     Consumed.with(Serdes.Long(), getRatingSerde(envProps)));

    getRatingAverageTable(ratingStream, avgRatingsTopicName, getCountAndSumSerde(envProps));

    // finish the topology
    return bldr.build();
  }
  //endregion

  public static SpecificAvroSerde<CountAndSum> getCountAndSumSerde(Properties envProps) {
    SpecificAvroSerde<CountAndSum> serde = new SpecificAvroSerde<>();
    serde.configure(getSerdeConfig(envProps), false);
    return serde;
  }

  public static SpecificAvroSerde<Rating> getRatingSerde(Properties envProps) {
    SpecificAvroSerde<Rating> serde = new SpecificAvroSerde<>();
    serde.configure(getSerdeConfig(envProps), false);
    return serde;
  }

  protected static Map<String, String> getSerdeConfig(Properties config) {
    final HashMap<String, String> map = new HashMap<>();

    final String srUrlConfig = config.getProperty(SCHEMA_REGISTRY_URL_CONFIG);
    map.put(SCHEMA_REGISTRY_URL_CONFIG, ofNullable(srUrlConfig).orElse(""));
    return map;
  }

  protected Properties loadEnvProperties() {
    final Config load = ConfigFactory.load();
    final Map<String, Object> map = load.entrySet()
        .stream()
        // ignore java.* and system properties
        .filter(entry -> Stream
            .of("java", "user", "sun", "os", "http", "ftp", "line", "file", "awt", "gopher", "socks", "path")
            .noneMatch(s -> entry.getKey().startsWith(s)))
        .peek(
            filteredEntry -> System.out.println(filteredEntry.getKey() + " : " + filteredEntry.getValue().unwrapped()))
        .collect(toMap(Map.Entry::getKey, y -> y.getValue().unwrapped()));
    Properties props = new Properties();
    props.putAll(map);
    return props;
  }

  public static void main(String[] args) {
    new RunningAverage().run();
  }
}
