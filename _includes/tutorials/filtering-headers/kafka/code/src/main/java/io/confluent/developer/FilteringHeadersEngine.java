package io.confluent.developer;

import io.confluent.developer.avro.Product;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Header;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;

public class FilteringHeadersEngine implements Runnable {

    private String inputTopic;
    private String outputTopic;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private KafkaConsumer<String, Product> consumer;
    private KafkaProducer<String, Product> producer;

    public FilteringHeadersEngine(String inputTopic, String outputTopic,
        KafkaConsumer<String, Product> consumer, KafkaProducer<String, Product> producer) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.consumer = consumer;
        this.producer = producer;
    }

    public void run() {

        try {
            consumer.subscribe(Arrays.asList(inputTopic));
            while (!closed.get()) {
                ConsumerRecords<String, Product> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, Product> record : records) {
                    Header header = record.headers().lastHeader("promotion");
                    String promotion = new String(header.value());
                    Product product = record.value();
                    if (promotion != null && Boolean.parseBoolean(promotion)) {
                        ProducerRecord<String, Product> filteredRecord =
                            new ProducerRecord<String, Product>(outputTopic, product);
                        producer.send(filteredRecord);
                    }
                }
            }
        } catch (WakeupException wue) {
            if (!closed.get()) throw wue;
        } finally {
            consumer.close();
            producer.close();
        }

    }

    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
    }

}