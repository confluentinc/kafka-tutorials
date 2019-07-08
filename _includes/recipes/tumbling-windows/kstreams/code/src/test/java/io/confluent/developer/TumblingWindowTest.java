package io.confluent.developer;

import io.confluent.developer.avro.Rating;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import static org.junit.Assert.*;

import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class TumblingWindowTest {
    private final static String TEST_CONFIG_FILE = "configuration/test.properties";


    private SpecificAvroSerializer<Rating> makeRatingSerializer(Properties envProps) {
        SpecificAvroSerializer<Rating> serializer = new SpecificAvroSerializer<>();

        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    private List<RatingCount> readOutputTopic(TopologyTestDriver testDriver,
                                              String outputTopic,
                                              Deserializer<String> keyDeserializer,
                                              Deserializer<String> valueDeserializer) {
        List<RatingCount> results = new ArrayList<>();

        while(true) {
            ProducerRecord<String, String> record = testDriver.readOutput(outputTopic, keyDeserializer, valueDeserializer);

            if (record != null) {
                results.add(new RatingCount(record.key().toString(), record.value()));
            } else {
                break;
            }
        }

        return results;
    }

    @Test
    public void testWindows() throws IOException {
        TumblingWindow tw = new TumblingWindow();
        Properties envProps = tw.loadEnvProperties(TEST_CONFIG_FILE);
        Properties streamProps = tw.buildStreamsProperties(envProps);

        String inputTopic = envProps.getProperty("rating.topic.name");
        String outputTopic = envProps.getProperty("rating.count.topic.name");

        Topology topology = tw.buildTopology(envProps);
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps);

        Serializer<String> stringSerializer = Serdes.String().serializer();
        SpecificAvroSerializer<Rating> ratingSerializer = makeRatingSerializer(envProps);
        Deserializer<String> stringDeserializer = Serdes.String().deserializer();

        ConsumerRecordFactory<String, Rating> ratingFactory = new ConsumerRecordFactory<>(stringSerializer, ratingSerializer);

        List<Rating> ratings = new ArrayList<>();
        ratings.add(Rating.newBuilder().setTitle("Die Hard").setReleaseYear(1988).setRating(8.2).setTimestamp("2019-04-25T18:00:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("Die Hard").setReleaseYear(1988).setRating(7.6).setTimestamp("2019-04-25T18:05:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("Tree of Life").setReleaseYear(2011).setRating(4.9).setTimestamp("2019-04-25T21:00:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("Tree of Life").setReleaseYear(2011).setRating(9.9).setTimestamp("2019-04-25T21:11:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("A Walk in the Clouds").setReleaseYear(1998).setRating(3.6).setTimestamp("2019-04-25T13:00:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("A Walk in the Clouds").setReleaseYear(1998).setRating(7.1).setTimestamp("2019-04-25T13:01:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("The Big Lebowski").setReleaseYear(1998).setRating(8.6).setTimestamp("2019-04-25T19:30:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("The Big Lebowski").setReleaseYear(1998).setRating(7.0).setTimestamp("2019-04-25T19:35:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("Super Mario Bros.").setReleaseYear(1993).setRating(3.5).setTimestamp("2019-04-25T11:15:00T-0000").build());
        ratings.add(Rating.newBuilder().setTitle("Super Mario Bros.").setReleaseYear(1993).setRating(2.0).setTimestamp("2019-04-25T11:40:00T-0000").build());

        List<RatingCount> ratingCounts = new ArrayList<>();
        ratingCounts.add(new RatingCount("Die Hard", "1"));
        ratingCounts.add(new RatingCount("Die Hard", "2"));
        ratingCounts.add(new RatingCount("Tree of Life", "1"));
        ratingCounts.add(new RatingCount("Tree of Life", "1"));
        ratingCounts.add(new RatingCount("A Walk in the Clouds", "1"));
        ratingCounts.add(new RatingCount("A Walk in the Clouds", "2"));
        ratingCounts.add(new RatingCount("The Big Lebowski", "1"));
        ratingCounts.add(new RatingCount("The Big Lebowski", "2"));
        ratingCounts.add(new RatingCount("Super Mario Bros.", "1"));
        ratingCounts.add(new RatingCount("Super Mario Bros.", "1"));

        for(Rating rating : ratings) {
            testDriver.pipeInput(ratingFactory.create(inputTopic, rating.getTitle(), rating));
        }

        List<RatingCount> actualOutput = readOutputTopic(testDriver,
                                                         outputTopic,
                                                         stringDeserializer,
                                                         stringDeserializer);

        assertEquals(ratingCounts.size(), actualOutput.size());
        for(int n = 0; n < ratingCounts.size(); n++) {
            assertEquals(ratingCounts.get(n).toString(), actualOutput.get(n).toString());
        }
    }
}

class RatingCount {
    private String key;
    private String value;

    public RatingCount(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return key + "=" + value;
    }
}