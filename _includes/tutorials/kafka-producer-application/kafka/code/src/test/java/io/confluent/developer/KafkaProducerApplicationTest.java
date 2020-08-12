package io.confluent.developer;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KeyValue;
import org.junit.Test;


public class KafkaProducerApplicationTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    @Test
    public void testProduce() throws IOException {
        final MockProducer<String, String> mockProducer = new MockProducer<>();
        final Properties props = KafkaProducerApplication.loadProperties(TEST_CONFIG_FILE);
        final String topic = props.getProperty("output.topic.name");
        final KafkaProducerApplication producerApp = new KafkaProducerApplication(mockProducer, topic);
        final List<String> records = Arrays.asList("foo-bar", "bar-foo", "baz-bar", "great:weather");

        records.forEach(producerApp::produce);

        final List<KeyValue<String, String>> expectedList = Arrays.asList(KeyValue.pair("foo", "bar"),
            KeyValue.pair("bar", "foo"),
            KeyValue.pair("baz", "bar"),
            KeyValue.pair("NO-KEY","great:weather"));

        final List<KeyValue<String, String>> actualList = mockProducer.history().stream().map(this::toKeyValue).collect(Collectors.toList());

        assertThat(actualList, equalTo(expectedList));
        producerApp.shutdown();
    }


    private KeyValue<String, String> toKeyValue(final ProducerRecord<String, String> producerRecord) {
        return KeyValue.pair(producerRecord.key(), producerRecord.value());
    }
}
