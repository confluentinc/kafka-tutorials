package io.confluent.developer;

import io.confluent.developer.avro.MovieTicketSales;
import io.confluent.developer.avro.YearlyMovieFigures;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.Test;
import java.io.IOException;
import java.util.*;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AggregatingMinMaxTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";

  private static SpecificAvroSerde<MovieTicketSales> makeMovieTicketSalesSerializer(
          Properties envProps, SchemaRegistryClient srClient) {

    SpecificAvroSerde<MovieTicketSales> serde = new SpecificAvroSerde<>(srClient);
    serde.configure(Collections.singletonMap(
            "schema.registry.url", envProps.getProperty("schema.registry.url")), false);
    return serde;
  }
  private static SpecificAvroSerde<YearlyMovieFigures> makeYearlyMovieFiguresSerializer(
          Properties envProps, SchemaRegistryClient srClient) {

    SpecificAvroSerde<YearlyMovieFigures> serde = new SpecificAvroSerde<>(srClient);
    serde.configure(Collections.singletonMap(
            "schema.registry.url", envProps.getProperty("schema.registry.url")), false);
    return serde;
  }

  @Test
  public void shouldCountTicketSales() throws IOException {

    Properties envProps = AggregatingMinMax.loadPropertiesFromConfigFile(TEST_CONFIG_FILE);
    Properties streamProps = AggregatingMinMax.buildStreamsProperties(envProps);

    final MockSchemaRegistryClient srClient = new MockSchemaRegistryClient();

    String inputTopic = envProps.getProperty("input.topic.name");
    String outputTopic = envProps.getProperty("output.topic.name");

    final SpecificAvroSerde<MovieTicketSales> movieTicketSerdes =
            makeMovieTicketSalesSerializer(envProps, srClient);
    final SpecificAvroSerde<YearlyMovieFigures> yearlyFiguresSerdes =
            makeYearlyMovieFiguresSerializer(envProps, srClient);

    final StreamsBuilder builder = new StreamsBuilder();
    Topology topology = AggregatingMinMax.buildTopology(builder, envProps, movieTicketSerdes, yearlyFiguresSerdes);

    try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps)) {

      TestInputTopic<String, MovieTicketSales> movieTicketSalesTestInputTopic = testDriver.createInputTopic(
              inputTopic, Serdes.String().serializer(), movieTicketSerdes.serializer());
      TestOutputTopic<Integer, YearlyMovieFigures> movieFiguresTestOutputTopic = testDriver.createOutputTopic(
              outputTopic, Serdes.Integer().deserializer(), yearlyFiguresSerdes.deserializer());

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Avengers: Endgame", 2019, 856980506));
      assertThat(
              movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2019, 856980506, 856980506))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Captain Marvel", 2019, 426829839));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2019, 426829839, 856980506))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Toy Story 4", 2019, 401486230));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2019, 401486230, 856980506))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("The Lion King", 2019, 385082142));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2019, 385082142, 856980506))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Black Panther", 2018, 700059566));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2018, 700059566,700059566))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Avengers: Infinity War", 2018, 678815482));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2018,678815482,700059566))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Deadpool 2", 2018,324512774));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2018,324512774,700059566))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Beauty and the Beast", 2017,517218368));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2017,517218368,517218368))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Wonder Woman", 2017,412563408));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2017,412563408,517218368))));

      movieTicketSalesTestInputTopic.pipeInput(
              new MovieTicketSales("Star Wars Ep. VIII: The Last Jedi", 2017,517218368));
      assertThat(movieFiguresTestOutputTopic.readValue(),
              is(equalTo(new YearlyMovieFigures(2017,412563408,517218368))));

      assertTrue(movieFiguresTestOutputTopic.isEmpty());
    }
  }
}
