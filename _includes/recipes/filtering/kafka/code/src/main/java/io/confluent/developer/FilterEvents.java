package io.confluent.developer;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import io.confluent.developer.avro.Publication;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;

public class FilterEvents {

    public Properties buildProducerProperties(Properties envProps) {

        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));

        return props;
    }

    public KafkaProducer<String, Publication> createProducer(Properties producerProps) {
        return new KafkaProducer<>(producerProps);
    }

    public Properties buildConsumerProperties(String groupId, Properties envProps) {
        Properties props = new Properties();

        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return props;
    }

    public KafkaConsumer<String, Publication> createConsumer(Properties consumerProps) {
        return new KafkaConsumer<>(consumerProps);
    }

    public void createTopics(Properties envProps) {

        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
        AdminClient client = AdminClient.create(config);

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
        client.close();

    }

    public Properties loadEnvProperties(String fileName) throws IOException {

        Properties envProps = new Properties();
        FileInputStream input = new FileInputStream(fileName);
        envProps.load(input);
        input.close();

        return envProps;
    }

    public void produceRecords(String inputTopic, List<Publication> inputPublications,
        KafkaProducer<String, Publication> producer) {

        ProducerRecord<String, Publication> record;

        for (Publication publication : inputPublications) {

            record = new ProducerRecord<>(inputTopic, publication);
            producer.send(record);

        }

    }

    public void applyFilter(String inputTopic,
        String outputTopic, KafkaConsumer<String, Publication> consumer,
        KafkaProducer<String, Publication> producer, String author) {

        consumer.subscribe(singletonList(inputTopic));
        ConsumerRecords<String, Publication> records = consumer.poll(ofMillis(5000));

        for (ConsumerRecord<String, Publication> record : records) {

            Publication publication = record.value();

            // Perform the filtering...
            if (publication.getName().equals(author)) {

                ProducerRecord<String, Publication> filteredRecord =
                    new ProducerRecord<>(outputTopic, publication);

                producer.send(filteredRecord);

            }
        }

    }

    public List<Publication> consumeRecords(String outputTopic,
        KafkaConsumer<String, Publication> consumer) {

        List<Publication> output = new ArrayList<>();
        consumer.subscribe(singletonList(outputTopic));

        ConsumerRecords<String, Publication> records = consumer.poll(ofMillis(5000));

        for (ConsumerRecord<String, Publication> record : records) {
            output.add(record.value());
        }

        return output;

    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        FilterEvents fe = new FilterEvents();
        Properties envProps = fe.loadEnvProperties(args[0]);
        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        fe.createTopics(envProps);

        Properties producerProps = fe.buildProducerProperties(envProps);
        KafkaProducer<String, Publication> producer = fe.createProducer(producerProps);
        Properties consumerProps = fe.buildConsumerProperties("inputGroup", envProps);
        KafkaConsumer<String, Publication> consumer = fe.createConsumer(consumerProps);

        final CountDownLatch latch = new CountDownLatch(1);

        // Attach shutdown handler to catch Control-C.
        Runtime.getRuntime().addShutdownHook(new Thread("kafka-shutdown-hook") {
            @Override
            public void run() {
                consumer.close();
                producer.close();
            }
        });

        try {

            consumer.subscribe(singletonList(inputTopic));

            while (true) {

                ConsumerRecords<String, Publication> records = consumer.poll(ofMillis(1000));
    
                for (ConsumerRecord<String, Publication> record : records) {
        
                    Publication publication = record.value();
        
                    // Perform the filtering...
                    if (publication.getName().equals("George R. R. Martin")) {
        
                        ProducerRecord<String, Publication> filteredRecord =
                            new ProducerRecord<>(outputTopic,
                                                 publication);
        
                        producer.send(filteredRecord);
        
                    }
        
                }

            }

        } catch (Throwable e) {
            System.exit(1);
        }

        System.exit(0);

    }

}
