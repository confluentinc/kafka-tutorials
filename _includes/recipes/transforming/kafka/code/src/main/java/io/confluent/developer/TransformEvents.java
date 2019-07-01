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

    public Properties loadEnvProperties(String fileName) throws IOException {

        Properties envProps = new Properties();
        FileInputStream input = new FileInputStream(fileName);
        envProps.load(input);
        input.close();

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

        try (AdminClient client = AdminClient.create(config)) {

            List<NewTopic> topics = new ArrayList<>();
            topics.add(new NewTopic(
                    envProps.getProperty("input.topic.name"),
                    Integer.parseInt(envProps.getProperty("input.topic.partitions")),
                    Short.parseShort(envProps.getProperty("input.topic.replication.factor"))));
            topics.add(new NewTopic(
                    envProps.getProperty("output.topic.name"),
                    Integer.parseInt(envProps.getProperty("output.topic.partitions")),
                    Short.parseShort(envProps.getProperty("output.topic.replication.factor"))));
    
            client.createTopics(topics);

        }

    }

    public void deleteTopics(Properties envProps) {

        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));

        try (AdminClient client = AdminClient.create(config)) {

            List<String> topics = new ArrayList<String>();
            topics.add(envProps.getProperty("input.topic.name"));
            topics.add(envProps.getProperty("output.topic.name"));
    
            client.deleteTopics(topics);

        }

    }

    public void applyTransformation(String inputTopic,
                                    String outputTopic,
                                    KafkaConsumer<String, RawMovie> consumer,
                                    KafkaProducer<String, Movie> producer) {

        consumer.subscribe(Arrays.asList(inputTopic));
        ConsumerRecords<String, RawMovie> records = consumer.poll(5000);

        for (ConsumerRecord<String, RawMovie> record : records) {

            Movie movie = convertRawMovie(record.value());
            ProducerRecord<String, Movie> transformedRecord =
                new ProducerRecord<String, Movie>(outputTopic, movie);

            producer.send(transformedRecord);

        }

    }

    public static Movie convertRawMovie(RawMovie rawMovie) {

        String titleParts[] = rawMovie.getTitle().split("::");
        String title = titleParts[0];
        int releaseYear = Integer.parseInt(titleParts[1]);

        return new Movie(rawMovie.getId(), title,
            releaseYear, rawMovie.getGenre());

    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        TransformEvents te = new TransformEvents();
        Properties envProps = te.loadEnvProperties(args[0]);
        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        te.createTopics(envProps);

        Properties producerProps = te.buildProducerProperties(envProps);
        KafkaProducer<String, RawMovie> rawProducer = te.createRawMovieProducer(producerProps);
        KafkaProducer<String, Movie> producer = te.createMovieProducer(producerProps);

        Properties consumerProps = te.buildConsumerProperties("inputGroup", envProps);
        KafkaConsumer<String, RawMovie> rawConsumer = te.createRawMovieConsumer(consumerProps);
        KafkaConsumer<String, Movie> consumer = te.createMovieConsumer(consumerProps);

        try {

        // Produce some movies in raw format...
        rawProducer.send(new ProducerRecord<String, RawMovie>(inputTopic,
            RawMovie.newBuilder()
            .setId(294)
            .setTitle("Die Hard::1988")
            .setGenre("action").build()));

        rawProducer.send(new ProducerRecord<String, RawMovie>(inputTopic,
            RawMovie.newBuilder()
            .setId(354)
            .setTitle("Tree of Life::2011")
            .setGenre("drama").build()));

        } finally {

            rawProducer.close();

        }

        // Apply the transformation...
        try {

            rawConsumer.subscribe(Arrays.asList(inputTopic));
            ConsumerRecords<String, RawMovie> records = rawConsumer.poll(5000);
    
            for (ConsumerRecord<String, RawMovie> record : records) {
    
                RawMovie rawMovie = record.value();
                Movie movie = convertRawMovie(rawMovie);

                ProducerRecord<String, Movie> transformedRecord =
                    new ProducerRecord<String, Movie>(outputTopic, movie);
    
                producer.send(transformedRecord);

            }

        } catch (Throwable e) {
            System.exit(1);
        } finally {

            rawConsumer.close();
            producer.close();

        }

        // Clean up and exit
        te.deleteTopics(envProps);
        System.exit(0);

    }

}