package io.confluent.developer.serialization;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import io.confluent.devx.kafka.config.ConfigLoader;
import io.confluent.devx.kafka.streams.TopologyVisualizer;


/**
 * Recipe interface.
 * Declares basic contract of all recipes.
 * Provides default implementation of `runRecipe` method (we're copy/pasting this stuff anyway)
 */
public interface IRecipe {

  Properties buildStreamsProperties(Properties envProps);

  Topology buildTopology(Properties envProps);

  void createTopics(Properties envProps);

  default void runRecipe(String configPath) {
    Properties envProps = ConfigLoader.loadConfig(configPath);
    Properties streamProps = this.buildStreamsProperties(envProps);
    Topology topology = this.buildTopology(envProps);

    this.createTopics(envProps);

    System.out.println("topology = " + TopologyVisualizer.visualize(String.valueOf(topology.describe())));

    final KafkaStreams streams = new KafkaStreams(topology, streamProps);
    final CountDownLatch latch = new CountDownLatch(1);

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);

  }
}

