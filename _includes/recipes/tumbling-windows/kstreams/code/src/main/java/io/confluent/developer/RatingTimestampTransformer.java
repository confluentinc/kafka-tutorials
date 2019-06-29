package io.confluent.developer;

import io.confluent.developer.avro.Rating;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.To;

public class RatingTimestampTransformer implements Transformer<String, Rating, Rating> {
    private ProcessorContext context;

    public void init(ProcessorContext context) {
        this.context = context;
    }

    public void close() {

    }

    public Rating transform(String title, Rating rating) {
        context.forward(title, rating, To.all().withTimestamp(rating.getTimestamp()));
        return null;
    }
}
