package io.confluent.developer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.confluent.developer.avro.PressureAlert;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static com.github.grantwest.eventually.EventuallyLambdaMatcher.eventuallyEval;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.streams.kstream.WindowedSerdes.timeWindowedSerdeFrom;
import static org.hamcrest.core.Is.is;

public class WindowFinalResultTest {

    private TopologyTestDriver topologyTestDriver;
    private SpecificAvroSerde<PressureAlert> pressureSerde;

    private Config config = ConfigFactory.load("test.properties");

    private String inputTopic = this.config.getString("input.topic.name");
    private String outputTopic = this.config.getString("output.topic.name");

    private Duration testWindowSize = config.getDuration("window.size");
    private Duration testGracePeriodSize = config.getDuration("window.grace.period");
    private Serde<Windowed<String>> keyResultSerde = timeWindowedSerdeFrom(String.class, testWindowSize.toMillis());

    private TimeWindows makeFixedTimeWindow() {
        return TimeWindows.of(testWindowSize).advanceBy(testWindowSize).grace(testGracePeriodSize);
    }

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

    private ProducerRecord<Windowed<String>, Long> readNext() {
        return topologyTestDriver.readOutput(
                this.outputTopic,
                this.keyResultSerde.deserializer(),
                Serdes.Long().deserializer()
        );
    }

    private List<ProducerRecord<Windowed<String>, Long>> readAtLeastNOutputs(int size) {
        List<ProducerRecord<Windowed<String>, Long>> result = new ArrayList<>();

        Assert.assertThat(() -> {
            ProducerRecord<Windowed<String>, Long> record = readNext();
            if(null != record) result.add(record);
            return result.size();
        }, eventuallyEval(is(size), this.testGracePeriodSize));

        return result;
    }

    @Before
    public void setUp() throws IOException, RestClientException {
        this.pressureSerde = makePressureAlertSerde();
        Topology topology = WindowFinalResult.buildTopology(config, makeFixedTimeWindow(), this.pressureSerde);
        this.topologyTestDriver = new TopologyTestDriver(topology, WindowFinalResult.buildProperties(config));
    }

    @After
    public void tearDown() {
        topologyTestDriver.close();
    }

    @Test
    public void topologyShouldGroupOverDatetimeWindows() {
        ConsumerRecordFactory<Bytes, PressureAlert> inputFactory =
                new ConsumerRecordFactory<>(Serdes.Bytes().serializer(), this.pressureSerde.serializer());

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
                this.topologyTestDriver.pipeInput(inputFactory.create(this.inputTopic, null, pressureAlert))
        );

        List<ProducerRecord<Windowed<String>, Long>> result = readAtLeastNOutputs(3);

        Optional<ProducerRecord<Windowed<String>, Long>> resultOne = result
                .stream().filter(Objects::nonNull).filter(r -> r.key().window().start() == 1569036600000L).findAny();
        Optional<ProducerRecord<Windowed<String>, Long>> resultTwo = result
                .stream().filter(Objects::nonNull).filter(r -> r.key().window().start() == 1569037500000L).findAny();
        Optional<ProducerRecord<Windowed<String>, Long>> resultThree = result
                .stream().filter(Objects::nonNull).filter(r -> r.key().window().start() == 1569038110000L).findAny();

        Assert.assertTrue(resultOne.isPresent());
        Assert.assertTrue(resultTwo.isPresent());
        Assert.assertTrue(resultThree.isPresent());

        Assert.assertEquals(3L, resultOne.get().value().longValue());
        Assert.assertEquals(2L, resultTwo.get().value().longValue());
        Assert.assertEquals(1L, resultThree.get().value().longValue());

        result.forEach((element) ->
                Assert.assertEquals(
                        makeFixedTimeWindow().size(),
                        element.key().window().end() - element.key().window().start()
                )
        );

        Assert.assertNull(readNext());
    }

    @Test
    public void topologyShouldGroupById() {
        ConsumerRecordFactory<Bytes, PressureAlert> inputFactory =
                new ConsumerRecordFactory<>(Serdes.Bytes().serializer(), this.pressureSerde.serializer());

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
                this.topologyTestDriver.pipeInput(inputFactory.create(this.inputTopic, null, pressureAlert))
        );

        List<ProducerRecord<Windowed<String>, Long>> result = readAtLeastNOutputs(3);

        Optional<ProducerRecord<Windowed<String>, Long>> resultOne =
                result.stream().filter(Objects::nonNull).filter(r -> r.key().key().equals("101")).findAny();
        Optional<ProducerRecord<Windowed<String>, Long>> resultTwo =
                result.stream().filter(Objects::nonNull).filter(r -> r.key().key().equals("102")).findAny();
        Optional<ProducerRecord<Windowed<String>, Long>> resultThree =
                result.stream().filter(Objects::nonNull).filter(r -> r.key().key().equals("103")).findAny();

        Assert.assertTrue(resultOne.isPresent());
        Assert.assertTrue(resultTwo.isPresent());
        Assert.assertTrue(resultThree.isPresent());

        Assert.assertEquals(3L, resultOne.get().value().longValue());
        Assert.assertEquals(3L, resultTwo.get().value().longValue());
        Assert.assertEquals(3L, resultThree.get().value().longValue());

        Assert.assertNull(readNext());
    }
}