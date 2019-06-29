package io.confluent.developer;

import io.confluent.developer.avro.Rating;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class RatingTimestampTransformerTest {

    @Test
    public void testTimestampExtraction() {
        RatingTimestampTransformer rtt = new RatingTimestampTransformer();
        MockProcessorContext context = new MockProcessorContext();

        Rating treeOfLife = Rating.newBuilder().setTitle("Tree of Life").setReleaseYear(2011).setRating(9.9).setTimestamp(1561734000).build();

        rtt.init(context);
        rtt.transform(treeOfLife.getTitle(), treeOfLife);

        List<MockProcessorContext.CapturedForward> results = context.forwarded();

        assertEquals(1, results.size());
        Rating transformedRating = (Rating)results.get(0).keyValue().value;
        assertEquals(treeOfLife.getTitle(), transformedRating.getTitle());
        assertEquals(treeOfLife.getReleaseYear(), transformedRating.getReleaseYear());
        assertEquals(treeOfLife.getRating(), transformedRating.getRating());
        assertEquals(treeOfLife.getTimestamp().longValue(), results.get(0).timestamp());
    }

}
