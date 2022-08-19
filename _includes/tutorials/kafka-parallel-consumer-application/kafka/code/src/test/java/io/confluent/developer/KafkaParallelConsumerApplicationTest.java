package io.confluent.developer;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.mockito.Mockito;



public class KafkaParallelConsumerApplicationTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  @Test
  public void consumerTest() throws Exception {

    final Path tempFilePath = Files.createTempFile("test-consumer-output", ".out");
    final ConsumerRecordHandler<String, String> recordsHandler = new FileWritingRecordHandler(tempFilePath);
    final Properties testConsumerProps = KafkaParallelConsumerApplication.loadProperties(TEST_CONFIG_FILE);
    final String topic = testConsumerProps.getProperty("input.topic.name");
    final TopicPartition topicPartition = new TopicPartition(topic, 0);
    final MockConsumer<String, String> mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
    final ParallelStreamProcessor<String, String> eosStreamProcessor = setupParallelConsumer(mockConsumer, topic);
    final KafkaParallelConsumerApplication consumerApplication = new KafkaParallelConsumerApplication(eosStreamProcessor, recordsHandler);
    mockConsumer.schedulePollTask(() -> addTopicPartitionsAssignmentAndAddConsumerRecords(topic, mockConsumer, topicPartition));
    mockConsumer.schedulePollTask(consumerApplication::shutdown);

    consumerApplication.runConsume(testConsumerProps);

    final List<String> expectedWords = Arrays.asList("foo", "bar", "baz");
    List<String> actualRecords = Files.readAllLines(tempFilePath);
    assertThat(actualRecords, equalTo(expectedWords));
  }

  private void addTopicPartitionsAssignmentAndAddConsumerRecords(final String topic,
                                 final MockConsumer<String, String> mockConsumer,
                                 final TopicPartition topicPartition) {

    final Map<TopicPartition, Long> beginningOffsets = new HashMap<>();
    beginningOffsets.put(topicPartition, 0L);
    mockConsumer.rebalance(Collections.singletonList(topicPartition));
    mockConsumer.updateBeginningOffsets(beginningOffsets);
    addConsumerRecords(mockConsumer, topic);
  }

  private void addConsumerRecords(final MockConsumer<String, String> mockConsumer, final String topic) {
    mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 0, null, "foo"));
    mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 1, null, "bar"));
    mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 2, null, "baz"));
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
