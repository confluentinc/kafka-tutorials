package io.confluent.developer;

import static org.junit.Assert.assertEquals;


import io.confluent.developer.avro.LoginEvent;
import io.confluent.developer.avro.LoginRollup;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Test;


public class CogroupingStreamsTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    @Test
    public void cogroupingTest() throws IOException {
        final CogroupingStreams instance = new CogroupingStreams();
        final Properties allProps = instance.loadEnvProperties(TEST_CONFIG_FILE);

        final String appOneInputTopicName = allProps.getProperty("app-one.topic.name");
        final String appTwoInputTopicName = allProps.getProperty("app-two.topic.name");
        final String appThreeInputTopicName = allProps.getProperty("app-three.topic.name");
        final String totalResultOutputTopicName = allProps.getProperty("output.topic.name");
      
        final Topology topology = instance.buildTopology(allProps);
        try (final TopologyTestDriver testDriver = new TopologyTestDriver(topology, allProps)) {

            final Serde<String> stringAvroSerde = Serdes.String();
            final SpecificAvroSerde<LoginEvent> loginEventSerde = CogroupingStreams.getSpecificAvroSerde(allProps);
            final SpecificAvroSerde<LoginRollup> rollupSerde = CogroupingStreams.getSpecificAvroSerde(allProps);

            final Serializer<String> keySerializer = stringAvroSerde.serializer();
            final Deserializer<String> keyDeserializer = stringAvroSerde.deserializer();
            final Serializer<LoginEvent> loginEventSerializer = loginEventSerde.serializer();


            final TestInputTopic<String, LoginEvent>  appOneInputTopic = testDriver.createInputTopic(appOneInputTopicName, keySerializer, loginEventSerializer);
            final TestInputTopic<String, LoginEvent>  appTwoInputTopic = testDriver.createInputTopic(appTwoInputTopicName, keySerializer, loginEventSerializer);
            final TestInputTopic<String, LoginEvent>  appThreeInputTopic = testDriver.createInputTopic(appThreeInputTopicName, keySerializer, loginEventSerializer);

            final TestOutputTopic<String, LoginRollup> outputTopic = testDriver.createOutputTopic(totalResultOutputTopicName, keyDeserializer, rollupSerde.deserializer());


            final List<LoginEvent> appOneEvents = new ArrayList<>();
            appOneEvents.add(LoginEvent.newBuilder().setAppId("one").setUserId("foo").setTime(5L).build());
            appOneEvents.add(LoginEvent.newBuilder().setAppId("one").setUserId("bar").setTime(6l).build());
            appOneEvents.add(LoginEvent.newBuilder().setAppId("one").setUserId("bar").setTime(7L).build());

            final List<LoginEvent> appTwoEvents = new ArrayList<>();
            appTwoEvents.add(LoginEvent.newBuilder().setAppId("two").setUserId("foo").setTime(5L).build());
            appTwoEvents.add(LoginEvent.newBuilder().setAppId("two").setUserId("foo").setTime(6l).build());
            appTwoEvents.add(LoginEvent.newBuilder().setAppId("two").setUserId("bar").setTime(7L).build());

            final List<LoginEvent> appThreeEvents = new ArrayList<>();
            appThreeEvents.add(LoginEvent.newBuilder().setAppId("three").setUserId("foo").setTime(5L).build());
            appThreeEvents.add(LoginEvent.newBuilder().setAppId("three").setUserId("foo").setTime(6l).build());
            appThreeEvents.add(LoginEvent.newBuilder().setAppId("three").setUserId("bar").setTime(7L).build());
            appThreeEvents.add(LoginEvent.newBuilder().setAppId("three").setUserId("bar").setTime(9L).build());

            final Map<String, Map<String, Long>> expectedEventRollups = new TreeMap<>();
            final Map<String, Long> expectedAppOneRollup = new HashMap<>();
            final LoginRollup expectedLoginRollup = new LoginRollup(expectedEventRollups);
            expectedAppOneRollup.put("foo", 1L);
            expectedAppOneRollup.put("bar", 2L);
            expectedEventRollups.put("one", expectedAppOneRollup);

            final Map<String, Long> expectedAppTwoRollup = new HashMap<>();
            expectedAppTwoRollup.put("foo", 2L);
            expectedAppTwoRollup.put("bar", 1L);
            expectedEventRollups.put("two", expectedAppTwoRollup);

            final Map<String, Long> expectedAppThreeRollup = new HashMap<>();
            expectedAppThreeRollup.put("foo", 2L);
            expectedAppThreeRollup.put("bar", 2L);
            expectedEventRollups.put("three", expectedAppThreeRollup);

            sendEvents(appOneEvents, appOneInputTopic);
            sendEvents(appTwoEvents, appTwoInputTopic);
            sendEvents(appThreeEvents, appThreeInputTopic);

            final List<LoginRollup> actualLoginEventResults = outputTopic.readValuesToList();
            final Map<String, Map<String, Long>> actualRollupMap = new HashMap<>();
            for (LoginRollup actualLoginEventResult : actualLoginEventResults) {
                  actualRollupMap.putAll(actualLoginEventResult.getLoginByAppAndUser());
            }
            final LoginRollup actualLoginRollup = new LoginRollup(actualRollupMap);

            assertEquals(expectedLoginRollup, actualLoginRollup);
        }
    }


    private void sendEvents(List<LoginEvent> events, TestInputTopic<String, LoginEvent> testInputTopic) {
        for (LoginEvent event : events) {
             testInputTopic.pipeInput(event.getAppId(), event);
        }
    }
}
