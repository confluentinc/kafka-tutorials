package io.confluent.developer;

import io.confluent.developer.avro.Rating;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import java.time.Duration;

public class RatingTimestampExtractor implements ValueTransformer<Rating, Rating> {

    private ProcessorContext context;

    public void init(ProcessorContext context) {
        this.context = context;
    }

    public Rating transform(Rating value) {
        // can access this.state
        return new Rating(); // or null
    }

    public void close() {
        // can access this.state
    }

}
