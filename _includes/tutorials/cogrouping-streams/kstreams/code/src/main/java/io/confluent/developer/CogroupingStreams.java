package io.confluent.developer;


import io.confluent.common.utils.TestUtils;
import io.confluent.developer.avro.LoginEvent;
import io.confluent.developer.avro.LoginRollup;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

public class CogroupingStreams {


	public Properties buildStreamsProperties(Properties envProps) {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("application.id"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024);
        return props;
    }

    public Topology buildTopology(Properties envProps) {
        final StreamsBuilder builder = new StreamsBuilder();
            final String appOneInputTopic = envProps.getProperty("app-one.topic.name");
            final String appTwoInputTopic = envProps.getProperty("app-two.topic.name");
            final String appThreeInputTopic = envProps.getProperty("app-three.topic.name");
            final String totalResultOutputTopic = envProps.getProperty("output.topic.name");

        final Serde<String> stringSerde = getPrimitiveAvroSerde(envProps, true);
        final Serde<LoginEvent> loginEventSerde = getSpecificAvroSerde(envProps);
        final Serde<LoginRollup> loginRollupSerde = getSpecificAvroSerde(envProps);


        final KStream<String, LoginEvent> appOneStream = builder.stream(appOneInputTopic, Consumed.with(stringSerde, loginEventSerde));
        final KStream<String, LoginEvent> appTwoStream = builder.stream(appTwoInputTopic, Consumed.with(stringSerde, loginEventSerde));
        final KStream<String, LoginEvent> appThreeStream = builder.stream(appThreeInputTopic, Consumed.with(stringSerde, loginEventSerde));

        final Aggregator<String, LoginEvent, LoginRollup> loginAggregator = new LoginAggregator();

        final KGroupedStream<String, LoginEvent> appOneGrouped = appOneStream.groupByKey();
        final KGroupedStream<String, LoginEvent> appTwoGrouped = appTwoStream.groupByKey();
        final KGroupedStream<String, LoginEvent> appThreeGrouped = appThreeStream.groupByKey();

        appOneGrouped.cogroup(loginAggregator)
            .cogroup(appTwoGrouped, loginAggregator)
            .cogroup(appThreeGrouped, loginAggregator)
            .aggregate(() -> new LoginRollup(new HashMap<>()), Materialized.with(Serdes.String(), loginRollupSerde))
            .toStream().to(totalResultOutputTopic, Produced.with(stringSerde, loginRollupSerde));

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    static <T> Serde<T> getPrimitiveAvroSerde(final Properties envProps, boolean isKey) {
        final KafkaAvroDeserializer deserializer = new KafkaAvroDeserializer();
        final KafkaAvroSerializer serializer = new KafkaAvroSerializer();
        final Map<String, String> config = new HashMap<>();
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, isKey);
        serializer.configure(config, isKey);
        return (Serde<T>)Serdes.serdeFrom(serializer, deserializer);
    }

    static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final Properties envProps) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();

        final HashMap<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                envProps.getProperty("schema.registry.url"));

        specificAvroSerde.configure(serdeConfig, false);
        return specificAvroSerde;
    }

    public void createTopics(final Properties envProps) {
        final Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
        try (final AdminClient client = AdminClient.create(config)) {

        final List<NewTopic> topics = new ArrayList<>();

            topics.add(new NewTopic(
                    envProps.getProperty("app-one.topic.name"),
                    Integer.parseInt(envProps.getProperty("app-one.topic.partitions")),
                    Short.parseShort(envProps.getProperty("app-one.topic.replication.factor"))));

            topics.add(new NewTopic(
                    envProps.getProperty("app-two.topic.name"),
                    Integer.parseInt(envProps.getProperty("app-two.topic.partitions")),
                    Short.parseShort(envProps.getProperty("app-two.topic.replication.factor"))));

            topics.add(new NewTopic(
                    envProps.getProperty("app-three.topic.name"),
                    Integer.parseInt(envProps.getProperty("app-three.topic.partitions")),
                    Short.parseShort(envProps.getProperty("app-three.topic.replication.factor"))));

            topics.add(new NewTopic(
                    envProps.getProperty("output.topic.name"),
                    Integer.parseInt(envProps.getProperty("output.topic.partitions")),
                    Short.parseShort(envProps.getProperty("output.topic.replication.factor"))));

            client.createTopics(topics);
        }
    }

    public Properties loadEnvProperties(String fileName) throws IOException {
        final Properties envProps = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        envProps.load(input);
        input.close();

        return envProps;
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        final CogroupingStreams instance = new CogroupingStreams();
        final Properties envProps = instance.loadEnvProperties(args[0]);
        final Properties streamProps = instance.buildStreamsProperties(envProps);
        final Topology topology = instance.buildTopology(envProps);

        instance.createTopics(envProps);

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
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

}
