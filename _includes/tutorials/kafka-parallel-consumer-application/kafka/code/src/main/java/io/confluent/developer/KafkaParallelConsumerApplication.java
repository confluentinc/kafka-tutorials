package io.confluent.developer;


import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.CommitMode.PERIODIC_CONSUMER_SYNC;
import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.KEY;

public class KafkaParallelConsumerApplication {
  private ConsumerRecordHandler<String, String> recordHandler;
  private ParallelStreamProcessor<String, String> parallelConsumer;

  public KafkaParallelConsumerApplication(final ParallelStreamProcessor<String, String> parallelConsumer,
                                          final ConsumerRecordHandler<String, String> recordsHandler) {
    this.parallelConsumer = parallelConsumer;
    this.recordHandler = recordsHandler;
  }

  public void runConsume(final Properties consumerProps) {
      parallelConsumer.subscribe(Collections.singletonList(consumerProps.getProperty("input.topic.name")));
      parallelConsumer.poll(context -> recordHandler.process(context.getSingleConsumerRecord()));
  }

  public void shutdown() {
    parallelConsumer.close();
  }

  public static Properties loadProperties(String fileName) throws IOException {
    final Properties props = new Properties();
    final FileInputStream input = new FileInputStream(fileName);
    props.load(input);
    input.close();
    return props;
  }

  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      throw new IllegalArgumentException(
          "This program takes one argument: the path to an environment configuration file.");
    }

    final Properties consumerAppProps = KafkaParallelConsumerApplication.loadProperties(args[0]);
    final String filePath = consumerAppProps.getProperty("file.path");
    final Consumer<String, String> consumer = new KafkaConsumer<>(consumerAppProps);
    final ParallelConsumerOptions options = ParallelConsumerOptions.<String, String>builder()
            .ordering(KEY)
            .maxConcurrency(1000)
            .consumer(consumer)
            .commitMode(PERIODIC_CONSUMER_SYNC)
            .build();
    ParallelStreamProcessor<String, String> eosStreamProcessor =
            ParallelStreamProcessor.createEosStreamProcessor(options);
    final ConsumerRecordHandler<String, String> recordsHandler = new FileWritingRecordHandler(Paths.get(filePath));
    final KafkaParallelConsumerApplication consumerApplication = new KafkaParallelConsumerApplication(eosStreamProcessor, recordsHandler);

    Runtime.getRuntime().addShutdownHook(new Thread(consumerApplication::shutdown));

    consumerApplication.runConsume(consumerAppProps);
  }

}
