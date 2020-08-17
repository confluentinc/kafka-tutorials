package io.confluent.developer;


import org.apache.avro.specific.SpecificRecord;
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
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.processor.TopicNameExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import io.confluent.common.utils.TestUtils;
import io.confluent.developer.avro.CompletedOrder;
import io.confluent.developer.avro.Order;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class DynamicOutputTopic {

    static final double FAKE_PRICE = 0.467423D;

	public Properties buildStreamsProperties(Properties envProps) {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("application.id"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());
        props.put(SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));

        return props;
    }

    public Topology buildTopology(Properties envProps) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String orderInputTopic = envProps.getProperty("input.topic.name");
        final String orderOutputTopic = envProps.getProperty("output.topic.name");
        final String specialOrderOutput = envProps.getProperty("special.order.topic.name");

        final Serde<Long> longSerde = getPrimitiveAvroSerde(envProps, true);
        final Serde<Order> orderSerde = getSpecificAvroSerde(envProps);
        final Serde<CompletedOrder> completedOrderSerde = getSpecificAvroSerde(envProps);

        final ValueMapper<Order, CompletedOrder> orderProcessingSimulator = v -> {
           double amount = v.getQuantity() * FAKE_PRICE;
           return CompletedOrder.newBuilder().setAmount(amount).setId(v.getId() + "-" + v.getSku()).setName(v.getName()).build();
        };

        final TopicNameExtractor<Long, CompletedOrder> orderTopicNameExtractor = (key, completedOrder, recordContext) -> {
              final String compositeId = completedOrder.getId();
              final String skuPart = compositeId.substring(compositeId.indexOf('-') + 1, 5);
              final String outTopic;
              if (skuPart.equals("QUA")) {
                  outTopic = specialOrderOutput;
              } else {
                  outTopic = orderOutputTopic;
              }
              return outTopic;
        };

        final KStream<Long, Order> exampleStream = builder.stream(orderInputTopic, Consumed.with(longSerde, orderSerde));

        exampleStream.mapValues(orderProcessingSimulator).to(orderTopicNameExtractor, Produced.with(longSerde, completedOrderSerde));

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    static <T> Serde<T> getPrimitiveAvroSerde(final Properties envProps, boolean isKey) {
        final KafkaAvroDeserializer deserializer = new KafkaAvroDeserializer();
        final KafkaAvroSerializer serializer = new KafkaAvroSerializer();
        final Map<String, String> config = new HashMap<>();
        config.put(SCHEMA_REGISTRY_URL_CONFIG,
                   envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, isKey);
        serializer.configure(config, isKey);
        return (Serde<T>)Serdes.serdeFrom(serializer, deserializer);
    }

    static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final Properties envProps) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();

        final HashMap<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG,
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
                    envProps.getProperty("input.topic.name"),
                    Integer.parseInt(envProps.getProperty("input.topic.partitions")),
                    Short.parseShort(envProps.getProperty("input.topic.replication.factor"))));

            topics.add(new NewTopic(
                    envProps.getProperty("output.topic.name"),
                    Integer.parseInt(envProps.getProperty("output.topic.partitions")),
                    Short.parseShort(envProps.getProperty("output.topic.replication.factor"))));

            topics.add(new NewTopic(
                envProps.getProperty("special.order.topic.name"),
                Integer.parseInt(envProps.getProperty("special.order.topic.partitions")),
                Short.parseShort(envProps.getProperty("special.order.topic.replication.factor"))));

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

        final DynamicOutputTopic instance = new DynamicOutputTopic();
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
