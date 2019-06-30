package io.confluent.developer;

import io.confluent.developer.avro.Rating;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.To;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class TumblingWindow {

    public Properties buildStreamsProperties(Properties envProps) {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("application.id"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, RatingTimestampExtractor.class.getName());
        return props;
    }

    public Topology buildTopology(Properties envProps) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String ratingTopic = envProps.getProperty("rating.topic.name");
        final String ratingCountTopic = envProps.getProperty("rating.count.topic.name");

        builder.<String, Rating>stream(ratingTopic)
            .map((key, rating) -> new KeyValue<>(rating.getTitle().toString(), rating))
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofDays(1)))
            .count()
            .toStream()
            .map((Windowed<String> key, Long count) -> new KeyValue(key.toString(), count.toString()))
            .to(ratingCountTopic, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    private SpecificAvroSerde<Rating> ratedMovieAvroSerde(Properties envProps) {
        SpecificAvroSerde<Rating> movieAvroSerde = new SpecificAvroSerde<>();

        final HashMap<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                envProps.getProperty("schema.registry.url"));

        movieAvroSerde.configure(serdeConfig, false);
        return movieAvroSerde;
    }

    public void createTopics(Properties envProps) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
        AdminClient client = AdminClient.create(config);

        List<NewTopic> topics = new ArrayList<>();

        topics.add(new NewTopic(
                envProps.getProperty("rating.topic.name"),
                Integer.parseInt(envProps.getProperty("rating.topic.partitions")),
                Short.parseShort(envProps.getProperty("rating.topic.replication.factor"))));

        topics.add(new NewTopic(
                envProps.getProperty("rating.count.topic.name"),
                Integer.parseInt(envProps.getProperty("rating.count.topic.partitions")),
                Short.parseShort(envProps.getProperty("rating.count.topic.replication.factor"))));

        client.createTopics(topics);
        client.close();
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
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        TumblingWindow tw = new TumblingWindow();
        Properties envProps = tw.loadEnvProperties(args[0]);
        Properties streamProps = tw.buildStreamsProperties(envProps);
        Topology topology = tw.buildTopology(envProps);

        tw.createTopics(envProps);

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
