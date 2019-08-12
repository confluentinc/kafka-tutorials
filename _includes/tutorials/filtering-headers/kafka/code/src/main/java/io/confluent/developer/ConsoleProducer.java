package io.confluent.developer;

import io.confluent.developer.avro.Product;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ConsoleProducer {

    private Properties loadEnvProperties(String fileName) {

        Properties envProps = new Properties();
        try (FileInputStream input = new FileInputStream(fileName)) {
            envProps.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return envProps;
        
    }

    private Properties buildProducerProperties(Properties envProps) {

        Properties props = new Properties();
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.getName());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));
        
        return props;

    }

    private KafkaProducer<String, Product> createProducer(Properties producerProps) {
        return new KafkaProducer<String, Product>(producerProps);
    }

    private void produceProducts(String inputTopic, List<Product> products,
                                 KafkaProducer<String, Product> producer) {
        ProducerRecord<String, Product> record = null;
        for (Product product : products) {
            // Products with a new price should have a header entry
            // that flags that product of being in promotion. The
            // 'FilteringHeadersEngine' will use that as criteria.
            if (product.getNewPrice() > 0) {
                record = new ProducerRecord<String, Product>(inputTopic,
                    null, null, product, Collections.singletonList(
                        new RecordHeader("promotion", "true".getBytes())));
            } else {
                record = new ProducerRecord<String, Product>(inputTopic,
                    null, null, product, Collections.singletonList(
                        new RecordHeader("promotion", "false".getBytes())));
            }
            producer.send(record);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("This program takes two arguments: the path to an environment configuration file.");
        }

        ConsoleProducer consoleProducer = new ConsoleProducer();
        Properties envProps = consoleProducer.loadEnvProperties(args[0]);
        String inputTopic = envProps.getProperty("input.topic.name");
        Properties producerProps = consoleProducer.buildProducerProperties(envProps);
        KafkaProducer<String, Product> producer = consoleProducer.createProducer(producerProps);

        try {
            List<Product> input = new ArrayList<>();
            input.add(new Product(1l, "ASUS ZenBook 13 Ultra-Slim Durable Laptop", 999.99, 768.99));
            input.add(new Product(2l, "VicTsing MM057 2.4G Wireless Mouse", 9.99, 5.99));
            input.add(new Product(3l, "Infinnet DisplayPort 1.2 Cable", 11.95, 9.99));
            input.add(new Product(4l, "TCL 55S425 55 inch 4K Smart LED", 329.99, 0.00));
            input.add(new Product(5l, "Blue Yeticaster Professional Broadcast Bundle", 168.38, 0.00));
            consoleProducer.produceProducts(inputTopic, input, producer);
        } finally {
            producer.close();
        }

    }

}