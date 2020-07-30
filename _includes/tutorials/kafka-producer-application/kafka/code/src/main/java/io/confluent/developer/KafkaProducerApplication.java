package io.confluent.developer;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaProducerApplication {

  private final Producer<String, String> producer;
  final String outTopic;

  public KafkaProducerApplication(final Producer<String, String> producer,
                                  final String topic) {
    this.producer = producer;
    outTopic = topic;
  }

  public Future<RecordMetadata> produce(final String message) {
    final String[] parts = message.split("#");
    final String key = parts[0];
    final String value = parts[1];
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

  public void printMetadata(Collection<Future<RecordMetadata>> metadata) {
    System.out.println("Offsets and timestamps committed in batch");
         metadata.forEach(m -> {
           try {
             final RecordMetadata recordMetadata = m.get();
             System.out.println("Offset " + recordMetadata.offset() + " timestamp " + recordMetadata.timestamp());
           } catch (InterruptedException | ExecutionException e) {
               if (e instanceof  InterruptedException) {
                  Thread.currentThread().interrupt();
               }
           }
         });
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      throw new IllegalArgumentException(
          "This program takes one argument: the path to an environment configuration file.");
    }

    final Properties props = KafkaProducerApplication.loadProperties(args[0]);
    final String topic = props.getProperty("output.topic.name");
    final Producer<String, String> producer = new KafkaProducer<>(props);
    final KafkaProducerApplication producerApp = new KafkaProducerApplication(producer, topic);

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(new Thread(producerApp::shutdown));

    try (final BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in))) {
      String filePath;
      System.out.println("Enter the file path to publish records > ");
      filePath = stdinReader.readLine();
      while (filePath != null && !filePath.equalsIgnoreCase("quit")) {
        try {
          List<String> linesToProduce = Files.readAllLines(Paths.get(filePath));;
          List<Future<RecordMetadata>> metadata = linesToProduce.stream().map(producerApp::produce).collect(Collectors.toList());
          producerApp.printMetadata(metadata);
          System.out.println("Enter the file path to publish records > ");
          filePath = stdinReader.readLine();
        } catch (IOException e) {
          System.err.println(String.format("Error reading file %s due to %s", filePath, e));
        }
      }
      producerApp.shutdown();
    }
  }
}
