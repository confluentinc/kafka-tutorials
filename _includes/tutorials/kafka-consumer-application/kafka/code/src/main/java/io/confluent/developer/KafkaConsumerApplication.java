package io.confluent.developer;


import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class KafkaConsumerApplication {

  private volatile boolean keepConsuming = true;
  private final Set<String> words = new HashSet<>();
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();


  public Properties buildProperties(Properties envProps) {
    Properties props = new Properties();

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
    props.put("topic", envProps.getProperty("input.topic.name"));
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-tutorial");

    return props;
  }

  public void runConsumer(final Properties consumerProps) {
    final Runnable consumerRunnable = () -> {
      try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
        consumer.subscribe(Collections.singletonList(consumerProps.getProperty("topic")));

        while (keepConsuming) {
          final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
          consumerRecords.forEach(record -> {
            final String value = record.value();
            System.out.println("Read record value [" + value + "]");
            words.add(value);
            if (value.equalsIgnoreCase("quit")) {
              keepConsuming = false;
            }
          });
        }
      }
    };
    executorService.submit(consumerRunnable);
  }
  // This method is not thread-safe, it's only
  // here for testing purposes.
  public Set<String> words() {
    return new HashSet<>(words);
  }

  public void shutdown() {
    keepConsuming = false;
    try {
      executorService.awaitTermination(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
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
      throw new IllegalArgumentException(
          "This program takes one argument: the path to an environment configuration file.");
    }

    final KafkaConsumerApplication consumerApplication = new KafkaConsumerApplication();
    final Properties envProps = consumerApplication.loadEnvProperties(args[0]);
    final Properties consumerProps = consumerApplication.buildProperties(envProps);
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consumerApplication.shutdown();
            countDownLatch.countDown();
    }));

    consumerApplication.runConsumer(consumerProps);
    countDownLatch.await();
  }

}
