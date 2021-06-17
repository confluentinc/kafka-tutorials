package io.confluent.developer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.confluent.developer.avro.PressureAlert;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.singletonMap;

public class PressureDatetimeExtractorTest {

    private TopologyTestDriver testDriver;
    private SpecificAvroSerde<PressureAlert> pressureSerde;

    private final Config config = ConfigFactory.load("test.properties");

    private final String inputTopic = this.config.getString("input.topic.name");
    private final String outputTopic = this.config.getString("output.topic.name");

    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern(this.config.getString("sensor.datetime.pattern"))
            .toFormatter();

    private final PressureDatetimeExtractor timestampExtractor = new PressureDatetimeExtractor(config);
    private TestOutputTopic<String, PressureAlert> testDriverOutputTopic;

    private SpecificAvroSerde<PressureAlert> makePressureAlertSerde() {

        Map<String, String> schemaRegistryConfigMap = singletonMap(
            SCHEMA_REGISTRY_URL_CONFIG,
            config.getString(SCHEMA_REGISTRY_URL_CONFIG)
        );

        SpecificAvroSerde<PressureAlert> serde = new SpecificAvroSerde<>();
        serde.configure(schemaRegistryConfigMap, false);
        return serde;
    }

    private List<TestRecord<String, PressureAlert>> readNOutputs(int size) {
        return testDriverOutputTopic.readRecordsToList();
    }

    @Before
    public void setUp() {
        this.pressureSerde = makePressureAlertSerde();

        Consumed<String, PressureAlert> consumedPressure =
            Consumed.with(Serdes.String(), pressureSerde)
                .withTimestampExtractor(timestampExtractor);

        Produced<String, PressureAlert> producedPressure =
            Produced.with(Serdes.String(), pressureSerde);

        StreamsBuilder builder = new StreamsBuilder();
        builder.stream(this.inputTopic, consumedPressure).to(this.outputTopic, producedPressure);

        this.testDriver = new TopologyTestDriver(builder.build(), WindowFinalResult.buildProperties(config));
        this.testDriverOutputTopic =
            testDriver
                .createOutputTopic(this.outputTopic, Serdes.String().deserializer(), this.pressureSerde.deserializer());
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void extract() {

        final TestInputTopic<Bytes, PressureAlert>
            testDriverInputTopic =
            testDriver.createInputTopic(this.inputTopic, Serdes.Bytes().serializer(), this.pressureSerde.serializer());
        List<PressureAlert> inputs = Arrays.asList(
                new PressureAlert("101", "2019-09-21T05:25:01.+0200", Integer.MAX_VALUE),
                new PressureAlert("102", "2019-09-21T05:30:02.+0200", Integer.MAX_VALUE),
                new PressureAlert("103", "2019-09-21T05:45:03.+0200", Integer.MAX_VALUE),
                new PressureAlert("104", "DEFINITELY-NOT-PARSABLE!!", Integer.MAX_VALUE),
                new PressureAlert("105", "1500-06-24T09:11:03.+0200", Integer.MAX_VALUE)
        );

        inputs.forEach(pressureAlert ->
                           testDriverInputTopic.pipeInput(null, pressureAlert)
        );

        List<TestRecord<String, PressureAlert>> result = readNOutputs(5);

        Optional<TestRecord<String, PressureAlert>> resultOne =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("101")).findFirst();
        Optional<TestRecord<String, PressureAlert>> resultTwo =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("102")).findFirst();
        Optional<TestRecord<String, PressureAlert>> resultThree =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("103")).findFirst();
        Optional<TestRecord<String, PressureAlert>> resultFour =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("104")).findFirst();
        Optional<TestRecord<String, PressureAlert>> resultFive =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("105")).findFirst();

        Assert.assertTrue(resultOne.isPresent());
        Assert.assertTrue(resultTwo.isPresent());
        Assert.assertTrue(resultThree.isPresent());
        Assert.assertFalse(resultFour.isPresent());
        Assert.assertFalse(resultFive.isPresent());

        Assert.assertEquals(
                formatter.parse("2019-09-21T05:25:01.+0200", Instant::from).toEpochMilli(),
                resultOne.get().timestamp().longValue()
        );

        Assert.assertEquals(
                formatter.parse("2019-09-21T05:30:02.+0200", Instant::from).toEpochMilli(),
                resultTwo.get().timestamp().longValue()
        );

        Assert.assertEquals(
                formatter.parse("2019-09-21T05:45:03.+0200", Instant::from).toEpochMilli(),
                resultThree.get().timestamp().longValue()
        );
    }
}
