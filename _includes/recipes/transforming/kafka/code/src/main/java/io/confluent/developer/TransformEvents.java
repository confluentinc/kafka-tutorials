package io.confluent.developer;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RawMovie;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class TransformEvents {

    public Properties loadEnvProperties(String fileName) {

        Properties envProps = new Properties();
        try (FileInputStream input = new FileInputStream(fileName)) {
            envProps.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return envProps;
        
    }

    public Properties buildProducerProperties(Properties envProps) {

        Properties props = new Properties();

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.getName());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));

        return props;

    }

    public Properties buildConsumerProperties(String groupId, Properties envProps) {

        Properties props = new Properties();

        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroDeserializer.class.getName());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));

        return props;

    }

    public KafkaConsumer<String, RawMovie> createRawMovieConsumer(Properties consumerProps) {
        return new KafkaConsumer<String, RawMovie>(consumerProps);
    }

    public KafkaConsumer<String, Movie> createMovieConsumer(Properties consumerProps) {
        return new KafkaConsumer<String, Movie>(consumerProps);
    }

    public KafkaProducer<String, Movie> createMovieProducer(Properties producerProps) {
        return new KafkaProducer<String, Movie>(producerProps);
    }

    public KafkaProducer<String, RawMovie> createRawMovieProducer(Properties producerProps) {
        return new KafkaProducer<String, RawMovie>(producerProps);
    }

    public void createTopics(Properties envProps) {

        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));

        try (AdminClient adminClient = AdminClient.create(config)) {

            List<NewTopic> topics = new ArrayList<>();
            topics.add(new NewTopic(
                    envProps.getProperty("input.topic.name"),
                    Integer.parseInt(envProps.getProperty("input.topic.partitions")),
                    Short.parseShort(envProps.getProperty("input.topic.replication.factor"))));
            topics.add(new NewTopic(
                    envProps.getProperty("output.topic.name"),
                    Integer.parseInt(envProps.getProperty("output.topic.partitions")),
                    Short.parseShort(envProps.getProperty("output.topic.replication.factor"))));
    
            adminClient.createTopics(topics);

        }

    }

    public void deleteTopics(Properties envProps) {

        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));

        try (AdminClient adminClient = AdminClient.create(config)) {

            List<String> topics = new ArrayList<String>();
            topics.add(envProps.getProperty("input.topic.name"));
            topics.add(envProps.getProperty("output.topic.name"));
    
            adminClient.deleteTopics(topics);

        }

    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        TransformEvents te = new TransformEvents();
        Properties envProps = te.loadEnvProperties(args[0]);
        te.deleteTopics(envProps);
        te.createTopics(envProps);
        
        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        Properties consumerProps = te.buildConsumerProperties("inputGroup", envProps);
        KafkaConsumer<String, RawMovie> rawConsumer = te.createRawMovieConsumer(consumerProps);
        Properties producerProps = te.buildProducerProperties(envProps);
        KafkaProducer<String, Movie> producer = te.createMovieProducer(producerProps);

        final TransformationEngine transEngine = new TransformationEngine(inputTopic,
            outputTopic, rawConsumer, producer);
        final Thread transEngineThread = new Thread(transEngine);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            transEngine.shutdown();
            latch.countDown();
        }));

        transEngineThread.start();

    }

}