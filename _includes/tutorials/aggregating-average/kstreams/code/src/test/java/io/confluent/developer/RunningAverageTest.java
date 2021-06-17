package io.confluent.developer;

import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import io.confluent.demo.CountAndSum;
import io.confluent.demo.Rating;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class RunningAverageTest {

  private static final String RATINGS_TOPIC_NAME = "ratings";
  private static final String AVERAGE_RATINGS_TOPIC_NAME = "average-ratings";
  private static final Rating LETHAL_WEAPON_RATING_10 = new Rating(362L, 10.0);
  private static final Rating LETHAL_WEAPON_RATING_8 = new Rating(362L, 8.0);

  private TopologyTestDriver testDriver;
  private SpecificAvroSerde<Rating> ratingSpecificAvroSerde;

  @Before
  public void setUp() {

    final Properties mockProps = new Properties();
    mockProps.put("application.id", "kafka-movies-test");
    mockProps.put("bootstrap.servers", "DUMMY_KAFKA_CONFLUENT_CLOUD_9092");
    mockProps.put("schema.registry.url", "mock://DUMMY_SR_CONFLUENT_CLOUD_8080");
    mockProps.put("default.topic.replication.factor", "1");
    mockProps.put("offset.reset.policy", "latest");
    mockProps.put("specific.avro.reader", true);

    final RunningAverage streamsApp = new RunningAverage();
    final Properties streamsConfig = streamsApp.buildStreamsProperties(mockProps);

    StreamsBuilder builder = new StreamsBuilder();

    SpecificAvroSerde<CountAndSum> countAndSumSerde = RunningAverage.getCountAndSumSerde(mockProps);
    ratingSpecificAvroSerde = RunningAverage.getRatingSerde(mockProps);

    KStream<Long, Rating> ratingStream = builder.stream(RATINGS_TOPIC_NAME,
                                                        Consumed.with(Serdes.Long(), ratingSpecificAvroSerde));

    RunningAverage.getRatingAverageTable(ratingStream,
                                         AVERAGE_RATINGS_TOPIC_NAME,
                                         countAndSumSerde);

    final Topology topology = builder.build();
    testDriver = new TopologyTestDriver(topology, streamsConfig);
  }

  @Test
  public void validateIfTestDriverCreated() {
    assertNotNull(testDriver);
  }

  @Test
  public void validateAverageRating() {

    TestInputTopic<Long, Rating> inputTopic = testDriver.createInputTopic(RATINGS_TOPIC_NAME,
                                                                          new LongSerializer(),
                                                                          ratingSpecificAvroSerde.serializer());

    inputTopic.pipeKeyValueList(asList(
        new KeyValue<>(LETHAL_WEAPON_RATING_8.getMovieId(), LETHAL_WEAPON_RATING_8),
        new KeyValue<>(LETHAL_WEAPON_RATING_10.getMovieId(), LETHAL_WEAPON_RATING_10)
    ));

    final TestOutputTopic<Long, Double> outputTopic = testDriver.createOutputTopic(AVERAGE_RATINGS_TOPIC_NAME,
                                                                                   new LongDeserializer(),
                                                                                   new DoubleDeserializer());

    final List<KeyValue<Long, Double>> keyValues = outputTopic.readKeyValuesToList();
    // I sent two records to input topic
    // I expect second record in topic will contain correct result
    final KeyValue<Long, Double> longDoubleKeyValue = keyValues.get(1);
    System.out.println("longDoubleKeyValue = " + longDoubleKeyValue);
    MatcherAssert.assertThat(longDoubleKeyValue,
               equalTo(new KeyValue<>(362L, 9.0)));

    final KeyValueStore<Long, Double>
        keyValueStore =
        testDriver.getKeyValueStore("average-ratings");
    final Double expected = keyValueStore.get(362L);
    Assert.assertEquals("Message", expected, 9.0, 0.0);
  }

  @After
  public void tearDown() {
    testDriver.close();
  }
}