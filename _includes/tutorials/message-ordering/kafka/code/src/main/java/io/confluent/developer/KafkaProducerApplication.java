package io.confluent.developer;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class KafkaProducerApplication {

    private final Producer<String, String> producer;
    final String outTopic;

    public KafkaProducerApplication(final Producer<String, String> producer,
                                    final String topic) {
        this.producer = producer;
        outTopic = topic;
    }

    public Future<RecordMetadata> produce(final String message) {
        final String[] parts = message.split("-");
        final String key, value;
        if (parts.length > 1) {
            key = parts[0];
            value = parts[1];
        } else {
            key = "NO-KEY";
            value = parts[0];
        }
        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(outTopic, key, value);
        return producer.send(producerRecord);
    }

    public void shutdown() {
        producer.close();
    }

    public static Properties loadProperties(String fileName) throws IOException {
        final Properties envProps = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        envProps.load(input);
        input.close();

        return envProps;
    }

    public void printMetadata(final Collection<Future<RecordMetadata>> metadata,
                              final String fileName) {
        System.out.println("Offsets and timestamps committed in batch from " + fileName);
        metadata.forEach(m -> {
            try {
                final RecordMetadata recordMetadata = m.get();
                System.out.println("Record written to offset " + recordMetadata.offset() + " timestamp " + recordMetadata.timestamp() + " partition " + recordMetadata.partition());
            } catch (InterruptedException | ExecutionException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "This program takes two arguments: the path to an environment configuration file and" +
                            "the path to the file with records to send");
        }

        final Properties props = KafkaProducerApplication.loadProperties(args[0]);

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        props.put(ProducerConfig.CLIENT_ID_CONFIG, "myApp");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        final String topic = props.getProperty("output.topic.name");
        final Producer<String, String> producer = new KafkaProducer<>(props);
        final KafkaProducerApplication producerApp = new KafkaProducerApplication(producer, topic);

        // Attach shutdown handler to catch Control-C.
        Runtime.getRuntime().addShutdownHook(new Thread(producerApp::shutdown));
        String filePath = args[1];
        try {
            List<String> linesToProduce = Files.readAllLines(Paths.get(filePath));
            List<Future<RecordMetadata>> metadata = linesToProduce.stream()
                    .filter(l -> !l.trim().isEmpty())
                    .map(producerApp::produce)
                    .collect(Collectors.toList());
            producerApp.printMetadata(metadata, filePath);

        } catch (IOException e) {
            System.err.println(String.format("Error reading file %s due to %s", filePath, e));
        }
        producerApp.shutdown();
    }
}
