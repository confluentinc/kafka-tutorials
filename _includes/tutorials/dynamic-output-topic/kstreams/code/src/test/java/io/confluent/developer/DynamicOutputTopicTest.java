package io.confluent.developer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.confluent.developer.avro.CompletedOrder;
import io.confluent.developer.avro.Order;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class DynamicOutputTopicTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  @Test
  public void shouldChooseCorrectOutputTopic() throws IOException {
    final DynamicOutputTopic instance = new DynamicOutputTopic();
    final Properties envProps = instance.loadEnvProperties(TEST_CONFIG_FILE);

    final Properties streamProps = instance.buildStreamsProperties(envProps);

    final String orderInputTopic = envProps.getProperty("input.topic.name");
    final String orderOutputTopic = envProps.getProperty("output.topic.name");
    final String specialOrderOutputTopic = envProps.getProperty("special.order.topic.name");

    final Topology topology = instance.buildTopology(envProps);
    try (final TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps)) {

      final Serde<Long> longAvroSerde = DynamicOutputTopic.getPrimitiveAvroSerde(envProps, true);
      final SpecificAvroSerde<Order> orderAvroSerde = DynamicOutputTopic.getSpecificAvroSerde(envProps);
      final SpecificAvroSerde<CompletedOrder>
          completedOrderAvroSerde =
          DynamicOutputTopic.getSpecificAvroSerde(envProps);

      final Serializer<Long> keySerializer = longAvroSerde.serializer();
      final Deserializer<Long> keyDeserializer = longAvroSerde.deserializer();
      final Serializer<Order> orderSerializer = orderAvroSerde.serializer();
      final Deserializer<CompletedOrder> completedOrderDeserializer = completedOrderAvroSerde.deserializer();

      final TestInputTopic<Long, Order>
          inputTopic =
          testDriver.createInputTopic(orderInputTopic, keySerializer, orderSerializer);
      final TestOutputTopic<Long, CompletedOrder>
          orderTopic =
          testDriver.createOutputTopic(orderOutputTopic, keyDeserializer, completedOrderDeserializer);
      final TestOutputTopic<Long, CompletedOrder>
          specialOrderTopic =
          testDriver.createOutputTopic(specialOrderOutputTopic, keyDeserializer, completedOrderDeserializer);

      final List<Order> orders = new ArrayList<>();
      orders.add(Order.newBuilder().setId(5L).setName("tp").setQuantity(10_000L).setSku("QUA00000123").build());
      orders.add(Order.newBuilder().setId(6L).setName("coffee").setQuantity(1_000L).setSku("COF0003456").build());
      orders.add(
          Order.newBuilder().setId(7L).setName("hand-sanitizer").setQuantity(6_000L).setSku("QUA000022334").build());
      orders.add(Order.newBuilder().setId(8L).setName("beer").setQuantity(4_000L).setSku("BER88899222").build());

      final List<CompletedOrder> expectedRegularCompletedOrders = new ArrayList<>();
      expectedRegularCompletedOrders.add(CompletedOrder.newBuilder().setName("coffee").setId("6-COF0003456")
                                             .setAmount(1_000L * DynamicOutputTopic.FAKE_PRICE).build());
      expectedRegularCompletedOrders.add(CompletedOrder.newBuilder().setName("beer").setId("8-BER88899222")
                                             .setAmount(4_000L * DynamicOutputTopic.FAKE_PRICE).build());

      final List<CompletedOrder> expectedSpecialOrders = new ArrayList<>();
      expectedSpecialOrders.add(CompletedOrder.newBuilder().setId("5-QUA00000123").setName("tp")
                                    .setAmount(10_000L * DynamicOutputTopic.FAKE_PRICE).build());
      expectedSpecialOrders.add(CompletedOrder.newBuilder().setId("7-QUA000022334").setName("hand-sanitizer")
                                    .setAmount(6_000L * DynamicOutputTopic.FAKE_PRICE).build());

      for (final Order order : orders) {
        inputTopic.pipeInput(order.getId(), order);
      }

      final List<CompletedOrder> actualRegularOrderResults = orderTopic.readValuesToList();
      final List<CompletedOrder> actualSpecialCompletedOrders = specialOrderTopic.readValuesToList();

      assertThat(expectedRegularCompletedOrders, equalTo(actualRegularOrderResults));
      assertThat(expectedSpecialOrders, equalTo(actualSpecialCompletedOrders));
    }
  }
}
