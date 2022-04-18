package io.confluent.developer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.confluent.developer.avro.PressureAlert;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.streams.kstream.WindowedSerdes.timeWindowedSerdeFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WindowFinalResultTest {

  private TopologyTestDriver testDriver;
  private TestOutputTopic<Windowed<String>, Long> testOutputTopic;
  private SpecificAvroSerde<PressureAlert> pressureSerde;

  private final Config config = ConfigFactory.load("test.properties");

  private final String inputTopic = this.config.getString("input.topic.name");
  private final String outputTopic = this.config.getString("output.topic.name");

  private final Duration testWindowSize = config.getDuration("window.size");
  private final Duration testGracePeriodSize = config.getDuration("window.grace.period");
  private final Serde<Windowed<String>> keyResultSerde = timeWindowedSerdeFrom(String.class, testWindowSize.toMillis());

  private TimeWindows makeFixedTimeWindow() {
    return TimeWindows.ofSizeAndGrace(testWindowSize,testGracePeriodSize).advanceBy(testWindowSize);
  }

  private SpecificAvroSerde<PressureAlert> makePressureAlertSerde() {

    Map<String, String> schemaRegistryConfigMap = Collections.singletonMap(
        SCHEMA_REGISTRY_URL_CONFIG,
        config.getString(SCHEMA_REGISTRY_URL_CONFIG)
    );

    SpecificAvroSerde<PressureAlert> serde = new SpecificAvroSerde<>();
    serde.configure(schemaRegistryConfigMap, false);

    return serde;
  }

  private List<TestRecord<Windowed<String>, Long>> readAtLeastNOutputs(int size) {
    final List<TestRecord<Windowed<String>, Long>> testRecords = testOutputTopic.readRecordsToList();
    assertThat(testRecords.size(), equalTo(size));

    return testRecords;
  }

  @Before
  public void setUp() {
    this.pressureSerde = makePressureAlertSerde();
    Topology topology = WindowFinalResult.buildTopology(config, makeFixedTimeWindow(), this.pressureSerde);
    this.testDriver = new TopologyTestDriver(topology, WindowFinalResult.buildProperties(config));
    this.testOutputTopic =
        testDriver.createOutputTopic(outputTopic, this.keyResultSerde.deserializer(), Serdes.Long().deserializer());
  }

  @After
  public void tearDown() {
    testDriver.close();
  }

  @Test
  public void topologyShouldGroupOverDatetimeWindows() {
    final TestInputTopic<Bytes, PressureAlert>
        testDriverInputTopic =
        testDriver.createInputTopic(this.inputTopic, Serdes.Bytes().serializer(), this.pressureSerde.serializer());

    List<PressureAlert> inputs = Arrays.asList(
        new PressureAlert("101", "2019-09-21T05:30:01.+0200", Integer.MAX_VALUE),
        new PressureAlert("101", "2019-09-21T05:30:02.+0200", Integer.MAX_VALUE),
        new PressureAlert("101", "2019-09-21T05:30:03.+0200", Integer.MAX_VALUE),
        new PressureAlert("101", "2019-09-21T05:45:01.+0200", Integer.MAX_VALUE),
        new PressureAlert("101", "2019-09-21T05:45:03.+0200", Integer.MAX_VALUE),
        new PressureAlert("101", "2019-09-21T05:55:10.+0200", Integer.MAX_VALUE),
        // ONE LAST EVENT TO TRIGGER TO MOVE THE STREAMING TIME
        new PressureAlert("XXX", "2019-09-21T05:55:40.+0200", Integer.MAX_VALUE)
    );

    inputs.forEach(pressureAlert ->
                       testDriverInputTopic.pipeInput(null, pressureAlert)
    );

    List<TestRecord<Windowed<String>, Long>> result = readAtLeastNOutputs(3);

    Optional<TestRecord<Windowed<String>, Long>> resultOne = result
        .stream().filter(Objects::nonNull).filter(r -> r.key().window().start() == 1569036600000L).findAny();
    Optional<TestRecord<Windowed<String>, Long>> resultTwo = result
        .stream().filter(Objects::nonNull).filter(r -> r.key().window().start() == 1569037500000L).findAny();
    Optional<TestRecord<Windowed<String>, Long>> resultThree = result
        .stream().filter(Objects::nonNull).filter(r -> r.key().window().start() == 1569038110000L).findAny();

    assertTrue(resultOne.isPresent());
    assertTrue(resultTwo.isPresent());
    assertTrue(resultThree.isPresent());

    assertEquals(3L, resultOne.get().value().longValue());
    assertEquals(2L, resultTwo.get().value().longValue());
    assertEquals(1L, resultThree.get().value().longValue());

    result.forEach((element) ->
                       assertEquals(
                           makeFixedTimeWindow().size(),
                           element.key().window().end() - element.key().window().start()
                       )
    );
  }

  @Test
  public void topologyShouldGroupById() {

    final TestInputTopic<Bytes, PressureAlert>
        testDriverInputTopic =
        testDriver.createInputTopic(this.inputTopic, Serdes.Bytes().serializer(), this.pressureSerde.serializer());

    List<PressureAlert> inputs = Arrays.asList(
        new PressureAlert("101", "2019-09-21T05:30:01.+0200", Integer.MAX_VALUE),
        new PressureAlert("101", "2019-09-21T05:30:02.+0200", Integer.MAX_VALUE),
        new PressureAlert("101", "2019-09-21T05:30:03.+0200", Integer.MAX_VALUE),
        new PressureAlert("102", "2019-09-21T05:30:01.+0200", Integer.MAX_VALUE),
        new PressureAlert("102", "2019-09-21T05:30:02.+0200", Integer.MAX_VALUE),
        new PressureAlert("102", "2019-09-21T05:30:03.+0200", Integer.MAX_VALUE),
        new PressureAlert("103", "2019-09-21T05:30:01.+0200", Integer.MAX_VALUE),
        new PressureAlert("103", "2019-09-21T05:30:02.+0200", Integer.MAX_VALUE),
        new PressureAlert("103", "2019-09-21T05:30:03.+0200", Integer.MAX_VALUE),
        // ONE LAST EVENT TO TRIGGER TO MOVE THE STREAMING TIME
        new PressureAlert("XXX", "2019-09-21T05:55:41.+0200", Integer.MAX_VALUE)
    );

    inputs.forEach(pressureAlert ->
                       testDriverInputTopic.pipeInput(null, pressureAlert)
    );

    List<TestRecord<Windowed<String>, Long>> result = readAtLeastNOutputs(3);

    Optional<TestRecord<Windowed<String>, Long>> resultOne =
        result.stream().filter(Objects::nonNull).filter(r -> r.key().key().equals("101")).findAny();
    Optional<TestRecord<Windowed<String>, Long>> resultTwo =
        result.stream().filter(Objects::nonNull).filter(r -> r.key().key().equals("102")).findAny();
    Optional<TestRecord<Windowed<String>, Long>> resultThree =
        result.stream().filter(Objects::nonNull).filter(r -> r.key().key().equals("103")).findAny();

    assertTrue(resultOne.isPresent());
    assertTrue(resultTwo.isPresent());
    assertTrue(resultThree.isPresent());

    assertEquals(3L, resultOne.get().value().longValue());
    assertEquals(3L, resultTwo.get().value().longValue());
    assertEquals(3L, resultThree.get().value().longValue());

    //Assert.assertNull(readNext());
  }
}
