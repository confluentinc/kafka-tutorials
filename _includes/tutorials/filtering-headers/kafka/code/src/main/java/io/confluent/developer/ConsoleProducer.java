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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        ConsoleProducer consoleProducer = new ConsoleProducer();
        Properties envProps = consoleProducer.loadEnvProperties(args[0]);
        String inputTopic = envProps.getProperty("input.topic.name");
        Properties producerProps = consoleProducer.buildProducerProperties(envProps);
        KafkaProducer<String, Product> producer = consoleProducer.createProducer(producerProps);

        List<Product> products = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Scanner scanner = new Scanner(System.in);

        try {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                products.add(objectMapper.readValue(line, Product.class));
            }
            consoleProducer.produceProducts(inputTopic, products, producer);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            producer.close();
        }

    }

}