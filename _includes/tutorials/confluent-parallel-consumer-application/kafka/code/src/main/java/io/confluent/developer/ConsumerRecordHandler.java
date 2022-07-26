package io.confluent.developer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ConsumerRecordHandler<K, V> {
   void process(ConsumerRecord<K, V> consumerRecord);
}
