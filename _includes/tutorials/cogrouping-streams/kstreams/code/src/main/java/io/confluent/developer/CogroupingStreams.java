package io.confluent.developer;


import io.confluent.common.utils.TestUtils;
import io.confluent.developer.avro.LoginEvent;
import io.confluent.developer.avro.LoginRollup;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
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

    public Topology buildTopology(Properties allProps) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String appOneInputTopic = allProps.getProperty("app-one.topic.name");
        final String appTwoInputTopic = allProps.getProperty("app-two.topic.name");
        final String appThreeInputTopic = allProps.getProperty("app-three.topic.name");
        final String totalResultOutputTopic = allProps.getProperty("output.topic.name");

        final Serde<LoginEvent> loginEventSerde = getSpecificAvroSerde(allProps);
        final Serde<LoginRollup> loginRollupSerde = getSpecificAvroSerde(allProps);


        final KStream<String, LoginEvent> appOneStream = builder.stream(appOneInputTopic, Consumed.with(Serdes.String(), loginEventSerde));
        final KStream<String, LoginEvent> appTwoStream = builder.stream(appTwoInputTopic, Consumed.with(Serdes.String(), loginEventSerde));
        final KStream<String, LoginEvent> appThreeStream = builder.stream(appThreeInputTopic, Consumed.with(Serdes.String(), loginEventSerde));

        final Aggregator<String, LoginEvent, LoginRollup> loginAggregator = new LoginAggregator();

        final KGroupedStream<String, LoginEvent> appOneGrouped = appOneStream.groupByKey();
        final KGroupedStream<String, LoginEvent> appTwoGrouped = appTwoStream.groupByKey();
        final KGroupedStream<String, LoginEvent> appThreeGrouped = appThreeStream.groupByKey();

        appOneGrouped.cogroup(loginAggregator)
            .cogroup(appTwoGrouped, loginAggregator)
            .cogroup(appThreeGrouped, loginAggregator)
            .aggregate(() -> new LoginRollup(new HashMap<>()), Materialized.with(Serdes.String(), loginRollupSerde))
            .toStream().to(totalResultOutputTopic, Produced.with(Serdes.String(), loginRollupSerde));

        return builder.build();
    }

    static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final Properties allProps) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
        final Map<String, String> serdeConfig = (Map)allProps;
        specificAvroSerde.configure(serdeConfig, false);
        return specificAvroSerde;
    }

    public void createTopics(final Properties allProps) {
        try (final AdminClient client = AdminClient.create(allProps)) {

        final List<NewTopic> topics = new ArrayList<>();

            topics.add(new NewTopic(
                    allProps.getProperty("app-one.topic.name"),
                    Integer.parseInt(allProps.getProperty("app-one.topic.partitions")),
                    Short.parseShort(allProps.getProperty("app-one.topic.replication.factor"))));

            topics.add(new NewTopic(
                    allProps.getProperty("app-two.topic.name"),
                    Integer.parseInt(allProps.getProperty("app-two.topic.partitions")),
                    Short.parseShort(allProps.getProperty("app-two.topic.replication.factor"))));

            topics.add(new NewTopic(
                    allProps.getProperty("app-three.topic.name"),
                    Integer.parseInt(allProps.getProperty("app-three.topic.partitions")),
                    Short.parseShort(allProps.getProperty("app-three.topic.replication.factor"))));

            topics.add(new NewTopic(
                    allProps.getProperty("output.topic.name"),
                    Integer.parseInt(allProps.getProperty("output.topic.partitions")),
                    Short.parseShort(allProps.getProperty("output.topic.replication.factor"))));

            client.createTopics(topics);
        }
    }

    public Properties loadEnvProperties(String fileName) throws IOException {
        final Properties allProps = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        allProps.load(input);
        input.close();

        return allProps;
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        final CogroupingStreams instance = new CogroupingStreams();
        final Properties allProps = instance.loadEnvProperties(args[0]);
        allProps.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());
        allProps.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024);
        final Topology topology = instance.buildTopology(allProps);

        instance.createTopics(allProps);

        final KafkaStreams streams = new KafkaStreams(topology, allProps);
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
