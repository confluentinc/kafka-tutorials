package io.confluent.developer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class FileWritingRecordHandler implements ConsumerRecordHandler<String, String> {

  private final Path path;

  public FileWritingRecordHandler(final Path path) {
    this.path = path;
  }

  @Override
  public void process(final ConsumerRecord<String, String> consumerRecord) {
    try {
      Files.write(path, consumerRecord.value().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
