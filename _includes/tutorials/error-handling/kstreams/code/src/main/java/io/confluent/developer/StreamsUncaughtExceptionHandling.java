package io.confluent.developer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class StreamsUncaughtExceptionHandling {

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
        final String outputTopic = envProps.getProperty("output.topic.name");

        final int maxFailures = Integer.parseInt(envProps.getProperty("max.failures"));
        final long maxTimeInterval = Long.parseLong(envProps.getProperty("max.time.millis"));

        builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> value.substring(0, value.indexOf('X')))
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), envProps);
        final MaxFailuresUncaughtExceptionHandler exceptionHandler = new MaxFailuresUncaughtExceptionHandler(maxFailures, maxTimeInterval);
        kafkaStreams.setUncaughtExceptionHandler(exceptionHandler);
        return builder.build();
    }

    public void createTopics(Properties envProps) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
        try (AdminClient client = AdminClient.create(config)) {
            List<NewTopic> topicList = new ArrayList<>();

            NewTopic sessionInput = new NewTopic(envProps.getProperty("input.topic.name"),
                    Integer.parseInt(envProps.getProperty("input.topic.partitions")),
                    Short.parseShort(envProps.getProperty("input.topic.replication.factor")));
            topicList.add(sessionInput);

            NewTopic counts = new NewTopic(envProps.getProperty("output.topic.name"),
                    Integer.parseInt(envProps.getProperty("output.topic.partitions")),
                    Short.parseShort(envProps.getProperty("output.topic.replication.factor")));

            topicList.add(counts);
            client.createTopics(topicList);
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
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        StreamsUncaughtExceptionHandling tw = new StreamsUncaughtExceptionHandling();
        Properties envProps = tw.loadEnvProperties(args[0]);
        Properties streamProps = tw.buildStreamsProperties(envProps);
        Topology topology = tw.buildTopology(envProps);

        tw.createTopics(envProps);
        TutorialDataGenerator dataGenerator = new TutorialDataGenerator(envProps);
        dataGenerator.generate();

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
            streams.cleanUp();
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    static class TutorialDataGenerator {
        final Properties properties;


        public TutorialDataGenerator(final Properties properties) {
            this.properties = properties;
        }

        public void generate() {
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

            try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
                String topic = properties.getProperty("input.topic.name");
                List<String> messages = Arrays.asList("comeX", "backX", "and fightX", "badValue", "it'sX", "justX", "aX","badValue", "fleshX", "woundX", "badValue");


                messages.forEach(message -> producer.send(new ProducerRecord<>(topic, message), (metadata, exception) -> {
                        if (exception != null) {
                            exception.printStackTrace(System.out);
                        } else {
                            System.out.printf("Produced record at offset %d to topic %s \n", metadata.offset(), metadata.topic());
                        }
                }));
            }
        }
    }
}
