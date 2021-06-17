package io.confluent.developer;


import io.confluent.common.utils.TestUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

public class StreamsToTable {


	public Properties buildStreamsProperties(Properties envProps) {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("application.id"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return props;
    }

    public Topology buildTopology(Properties envProps) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String inputTopic = envProps.getProperty("input.topic.name");
        final String streamsOutputTopic = envProps.getProperty("streams.output.topic.name");
        final String tableOutputTopic = envProps.getProperty("table.output.topic.name");

        final Serde<String> stringSerde = Serdes.String();

        final KStream<String, String> stream = builder.stream(inputTopic, Consumed.with(stringSerde, stringSerde));

        final KTable<String, String> convertedTable = stream.toTable(Materialized.as("stream-converted-to-table"));

        stream.to(streamsOutputTopic, Produced.with(stringSerde, stringSerde));
        convertedTable.toStream().to(tableOutputTopic, Produced.with(stringSerde, stringSerde));


        return builder.build();
    }


    public void createTopics(final Properties envProps) {
        final Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
        try (final AdminClient client = AdminClient.create(config)) {

        final List<NewTopic> topics = new ArrayList<>();

            topics.add(new NewTopic(
                    envProps.getProperty("input.topic.name"),
                    Integer.parseInt(envProps.getProperty("input.topic.partitions")),
                    Short.parseShort(envProps.getProperty("input.topic.replication.factor"))));

            topics.add(new NewTopic(
                    envProps.getProperty("streams.output.topic.name"),
                    Integer.parseInt(envProps.getProperty("streams.output.topic.partitions")),
                    Short.parseShort(envProps.getProperty("streams.output.topic.replication.factor"))));

            topics.add(new NewTopic(
                envProps.getProperty("table.output.topic.name"),
                Integer.parseInt(envProps.getProperty("table.output.topic.partitions")),
                Short.parseShort(envProps.getProperty("table.output.topic.replication.factor"))));

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

        final StreamsToTable instance = new StreamsToTable();
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
            streams.cleanUp();
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

}
