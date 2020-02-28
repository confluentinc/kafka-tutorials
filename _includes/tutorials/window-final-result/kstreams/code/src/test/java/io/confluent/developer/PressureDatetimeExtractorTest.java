package io.confluent.developer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.confluent.developer.avro.PressureAlert;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class PressureDatetimeExtractorTest {

    private TopologyTestDriver topologyTestDriver;
    private SpecificAvroSerde<PressureAlert> pressureSerde;

    private Config config = ConfigFactory.load("test.properties");

    private String inputTopic = this.config.getString("input.topic.name");
    private String outputTopic = this.config.getString("output.topic.name");

    private DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern(this.config.getString("sensor.datetime.pattern"))
            .toFormatter();

    private final PressureDatetimeExtractor timestampExtractor = new PressureDatetimeExtractor(config);

    private SpecificAvroSerde<PressureAlert> makePressureAlertSerde() throws IOException, RestClientException {

        final MockSchemaRegistryClient client = new MockSchemaRegistryClient();

        String subject = String.format("%s-value", config.getString("input.topic.name"));

        client.register(subject, PressureAlert.SCHEMA$);

        Map<String, String> schemaRegistryConfigMap = Collections.singletonMap(
                SCHEMA_REGISTRY_URL_CONFIG,
                config.getString(SCHEMA_REGISTRY_URL_CONFIG)
        );

        SpecificAvroSerde<PressureAlert> serde = new SpecificAvroSerde<>(client);

        serde.configure(schemaRegistryConfigMap, false);

        return serde;
    }

    private List<ProducerRecord<String, PressureAlert>> readNOutputs(int size) {
        return IntStream.range(1, size).mapToObj((notUsedInt) ->
                topologyTestDriver.readOutput(
                        this.outputTopic,
                        Serdes.String().deserializer(),
                        this.pressureSerde.deserializer()
                )
        ).collect(Collectors.toList());
    }

    @Before
    public void setUp() throws Exception {
        this.pressureSerde = makePressureAlertSerde();

        Consumed<String, PressureAlert> consumedPressure = Consumed
                .with(Serdes.String(), pressureSerde)
                .withTimestampExtractor(timestampExtractor);

        Produced<String, PressureAlert> producedPressure = Produced.with(Serdes.String(), pressureSerde);

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(this.inputTopic, consumedPressure).to(this.outputTopic, producedPressure);

        this.topologyTestDriver = new TopologyTestDriver(builder.build(), WindowFinalResult.buildProperties(config));
    }

    @After
    public void tearDown() throws Exception {
        topologyTestDriver.close();
    }

    @Test
    public void extract() {
        ConsumerRecordFactory<Bytes, PressureAlert> inputFactory =
                new ConsumerRecordFactory<>(Serdes.Bytes().serializer(), this.pressureSerde.serializer());

        List<PressureAlert> inputs = Arrays.asList(
                new PressureAlert("101", "2019-09-21T05:25:01.+0200", Integer.MAX_VALUE),
                new PressureAlert("102", "2019-09-21T05:30:02.+0200", Integer.MAX_VALUE),
                new PressureAlert("103", "2019-09-21T05:45:03.+0200", Integer.MAX_VALUE),
                new PressureAlert("104", "DEFINITELY-NOT-PARSABLE!!", Integer.MAX_VALUE),
                new PressureAlert("105", "1500-06-24T09:11:03.+0200", Integer.MAX_VALUE)
        );

        inputs.forEach(pressureAlert ->
                this.topologyTestDriver.pipeInput(inputFactory.create(this.inputTopic, null, pressureAlert))
        );

        List<ProducerRecord<String, PressureAlert>> result = readNOutputs(5);

        Optional<ProducerRecord<String, PressureAlert>> resultOne =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("101")).findFirst();
        Optional<ProducerRecord<String, PressureAlert>> resultTwo =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("102")).findFirst();
        Optional<ProducerRecord<String, PressureAlert>> resultThree =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("103")).findFirst();
        Optional<ProducerRecord<String, PressureAlert>> resultFour =
                result.stream().filter(Objects::nonNull).filter(r -> r.value().getId().equals("104")).findFirst();
        Optional<ProducerRecord<String, PressureAlert>> resultFive =
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
