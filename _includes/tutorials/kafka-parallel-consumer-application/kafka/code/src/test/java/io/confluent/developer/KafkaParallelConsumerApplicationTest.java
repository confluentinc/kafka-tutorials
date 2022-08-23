package io.confluent.developer;

import io.confluent.csid.utils.LongPollingMockConsumer;
import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.awaitility.Awaitility;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.KEY;
import static java.util.concurrent.TimeUnit.SECONDS;


public class KafkaParallelConsumerApplicationTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  @Test
  public void consumerTest() throws Exception {
    final Path tempFilePath = Files.createTempFile("test-consumer-output", ".out");
    final ConsumerRecordHandler<String, String> recordsHandler = new FileWritingRecordHandler(tempFilePath);
    final Properties testConsumerProps = KafkaParallelConsumerApplication.loadProperties(TEST_CONFIG_FILE);
    final String topic = testConsumerProps.getProperty("input.topic.name");

    final LongPollingMockConsumer<String, String> mockConsumer = new LongPollingMockConsumer<>(OffsetResetStrategy.EARLIEST);
    final ParallelStreamProcessor<String, String> eosStreamProcessor = setupParallelConsumer(mockConsumer, topic);
    final KafkaParallelConsumerApplication consumerApplication = new KafkaParallelConsumerApplication(eosStreamProcessor, recordsHandler);
    mockConsumer.subscribeWithRebalanceAndAssignment(Collections.singletonList(topic), 1);
    mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 0, null, "foo"));
    mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 1, null, "bar"));
    mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 2, null, "baz"));

    consumerApplication.runConsume(testConsumerProps);

    final List<String> expectedRecords = Arrays.asList("foo", "bar", "baz");
    Awaitility.await()
            .atMost(5, SECONDS)
            .pollInterval(Duration.ofSeconds(1))
            .until(() -> {
              List<String> actualRecords = Files.readAllLines(tempFilePath);
              return actualRecords.equals(expectedRecords);
            });
  }


  private ParallelStreamProcessor<String, String> setupParallelConsumer(MockConsumer<String, String> mockConsumer, final String topic) {
    ParallelConsumerOptions options = ParallelConsumerOptions.<String, String>builder()
            .ordering(KEY) // <2>
            .maxConcurrency(1000) // <3>
            .consumer(mockConsumer)
            .build();

    ParallelStreamProcessor<String, String> eosStreamProcessor =
            ParallelStreamProcessor.createEosStreamProcessor(options);

    eosStreamProcessor.subscribe(Collections.singletonList(topic));

    return eosStreamProcessor;
  }

}
