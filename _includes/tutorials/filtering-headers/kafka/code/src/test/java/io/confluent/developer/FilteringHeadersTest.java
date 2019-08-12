package io.confluent.developer;

import io.confluent.developer.avro.Product;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.ClassRule;
import org.junit.Rule;
import org.testcontainers.containers.KafkaContainer;
import io.confluent.testcontainers.SchemaRegistryContainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.time.Duration.ofMillis;

public class FilteringHeadersTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";
    private final static Properties ENVIRONMENT_PROPERTIES = loadEnvironmentProperties();

    @ClassRule
    public static KafkaContainer kafkaContainer = new KafkaContainer(
        ENVIRONMENT_PROPERTIES.getProperty("confluent.version"));

    @Rule
    public SchemaRegistryContainer schemaRegistryContainer =
        new SchemaRegistryContainer(ENVIRONMENT_PROPERTIES
            .getProperty("confluent.version"))
            .withKafka(kafkaContainer);

    private String inputTopic, outputTopic;
    private FilteringHeadersEngine filteringHeadersEngine;
    private KafkaProducer<String, Product> producer;
    private KafkaConsumer<String, Product> consumer;
    private KafkaConsumer<String, Product> consumerEngine;

    @Before
    public void initialize() {

        FilteringHeaders filteringHeaders = new FilteringHeaders();
        ENVIRONMENT_PROPERTIES.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        ENVIRONMENT_PROPERTIES.put("schema.registry.url", schemaRegistryContainer.getTarget());
        filteringHeaders.createTopics(ENVIRONMENT_PROPERTIES);

        inputTopic = ENVIRONMENT_PROPERTIES.getProperty("input.topic.name");
        outputTopic = ENVIRONMENT_PROPERTIES.getProperty("output.topic.name");
        Properties producerProps = filteringHeaders.buildProducerProperties(ENVIRONMENT_PROPERTIES);
        Properties consumerProps = filteringHeaders.buildConsumerProperties("FilteredProducts", ENVIRONMENT_PROPERTIES);
        Properties consumerEngineProps = filteringHeaders.buildConsumerProperties("FilterProductsEngine", ENVIRONMENT_PROPERTIES);

        consumer = filteringHeaders.createConsumer(consumerProps);
        consumerEngine = filteringHeaders.createConsumer(consumerEngineProps);
        producer = filteringHeaders.createProducer(producerProps);

    }

    @After
    public void tearDown() {
        filteringHeadersEngine.shutdown();
    }

    @Test
    public void checkWhichProductsHasPromotions() {

        List<Product> input = new ArrayList<>();
        input.add(new Product(1l, "ASUS ZenBook 13 Ultra-Slim Durable Laptop", 999.99, 768.99));
        input.add(new Product(2l, "VicTsing MM057 2.4G Wireless Mouse", 9.99, 5.99));
        input.add(new Product(3l, "Infinnet DisplayPort 1.2 Cable", 11.95, 9.99));
        input.add(new Product(4l, "TCL 55S425 55 inch 4K Smart LED", 329.99, 0.00));
        input.add(new Product(5l, "Blue Yeticaster Professional Broadcast Bundle", 168.38, 0.00));

        List<Product> expectedOutput = new ArrayList<>();
        expectedOutput.add(new Product(1l, "ASUS ZenBook 13 Ultra-Slim Durable Laptop", 999.99, 768.99));
        expectedOutput.add(new Product(2l, "VicTsing MM057 2.4G Wireless Mouse", 9.99, 5.99));
        expectedOutput.add(new Product(3l, "Infinnet DisplayPort 1.2 Cable", 11.95, 9.99));

        filteringHeadersEngine = new FilteringHeadersEngine(
            inputTopic, outputTopic, consumerEngine, producer);

        Thread filteringHeadersThread = new Thread(filteringHeadersEngine);
        List<Product> actualOutput = null;

        try {
            filteringHeadersThread.start();
            // Produce all the products for the testing process...
            produceProducts(inputTopic, input, producer);
            // Read the transformed records from the output topic,
            // that has been put there by the filtering engine.
            actualOutput = consumeProducts(outputTopic, consumer);
        } finally {
            filteringHeadersEngine.shutdown();
        }
        
        Assert.assertEquals(expectedOutput, actualOutput);
        
    }

    private List<Product> consumeProducts(String outputTopic,
                                        KafkaConsumer<String, Product> consumer) {

        // Wait five seconds until all the records gets persisted, to
        // avoid a race condition between producers and consumers...
        try { Thread.sleep(5000); } catch (Exception ex) {}
        
        List<Product> output = new ArrayList<>();
        consumer.subscribe(Arrays.asList(outputTopic));
        ConsumerRecords<String, Product> records = consumer.poll(ofMillis(1000));

        for (ConsumerRecord<String, Product> record : records) {
            output.add(record.value());
        }

        return output;

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

    private static Properties loadEnvironmentProperties() {
        Properties environmentProps = new Properties();
        try (FileInputStream input = new FileInputStream(TEST_CONFIG_FILE)) {
            environmentProps.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return environmentProps;
    }

}