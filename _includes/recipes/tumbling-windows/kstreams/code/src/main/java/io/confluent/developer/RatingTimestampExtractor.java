package io.confluent.developer;

import io.confluent.developer.avro.Rating;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class RatingTimestampExtractor implements TimestampExtractor {
    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        return ((Rating)record.value()).getTimestamp();
    }
}
