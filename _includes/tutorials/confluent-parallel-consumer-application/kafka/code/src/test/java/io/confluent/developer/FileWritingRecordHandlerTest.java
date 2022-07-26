package io.confluent.developer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FileWritingRecordHandlerTest {

  @Test
  public void testProcess() throws IOException {
    final Path tempFilePath = Files.createTempFile("test-handler", ".out");
    try {
      final ConsumerRecordHandler<String, String> recordHandler = new FileWritingRecordHandler(tempFilePath);
      recordHandler.process(createConsumerRecord());
      final List<String> expectedWords = Arrays.asList("my record");
      List<String> actualRecords = Files.readAllLines(tempFilePath);
      assertThat(actualRecords, equalTo(expectedWords));
    } finally {
      Files.deleteIfExists(tempFilePath);
    }
  }

  private ConsumerRecord<String, String> createConsumerRecord() {
    final String topic = "test";
    final int partition = 0;
    return new ConsumerRecord<>(topic, partition, 0, null, "my record");
  }
}
