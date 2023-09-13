package io.confluent.developer;


import com.google.common.io.Resources;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.client.JobCancellationException;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.assertj.core.util.Sets;
import org.junit.BeforeClass;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.testcontainers.containers.KafkaContainer.KAFKA_PORT;

/**
 * Base class for Flink SQL integration tests that use Flink's Kafka connectors. Encapsulates
 * Kafka broker and Schema Registry Testcontainer management and includes utility methods for
 * dynamically configuring Flink SQL Kafka connectors and processing Table API results.
 */
public class AbstractFlinkKafkaTest {

  protected static StreamTableEnvironment streamTableEnv;
  protected static Integer schemaRegistryPort, kafkaPort;

  @BeforeClass
  public static void setup() {
    // create Flink table environment that test subclasses will use to execute SQL statements
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(4);
    env.getConfig().setRestartStrategy(RestartStrategies.noRestart());
    env.setStateBackend(new EmbeddedRocksDBStateBackend());
    streamTableEnv = StreamTableEnvironment.create(env, EnvironmentSettings.newInstance().inStreamingMode().build());


    // Start Kafka and Schema Registry Testcontainers. Set the exposed ports that test subclasses
    // can use to dynamically configure Kafka connectors. Schema Registry enables connectors to
    // be configured with 'value.format' = 'avro-confluent'
    Network network = Network.newNetwork();

    KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.2"))
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_NUM_PARTITIONS", "1")
        .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "500")
        .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
        .withReuse(true)
        .withNetwork(network);
    kafka.start();
    kafkaPort = kafka.getMappedPort(KAFKA_PORT);

    GenericContainer schemaRegistry = new GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.3.2"))
        .withExposedPorts(8081)
        .withNetwork(kafka.getNetwork())
        .withEnv("SCHEMA_REGISTRY_HOST_NAME", "localhost")
        .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
        .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + kafka.getNetworkAliases().get(0) + ":9092");
    schemaRegistry.start();
    schemaRegistryPort = schemaRegistry.getMappedPort(8081);
  }

  /**
   * Given a resource filename and optional Kafka / Schema Registry ports, return the resource
   * file contents as a String with ports substituted for KAFKA_PORT and SCHEMA_REGISTRY_PORT
   * placeholders.
   *
   * @param resourceFileName    the resource file name
   * @param kafkaPort           the port that Kafka broker exposes
   * @param schemaRegistryPort  the port that Schema Registry exposes
   * @return resource file contents with port values substituted for placeholders
   * @throws IOException if resource file can't be read
   */
  protected static String getResourceFileContents(
      String resourceFileName,
      Optional<Integer> kafkaPort,
      Optional<Integer> schemaRegistryPort
  ) throws IOException {
    URL url = Resources.getResource(resourceFileName);
    String contents = Resources.toString(url, StandardCharsets.UTF_8);
    if (kafkaPort.isPresent()) {
      contents = contents.replaceAll("KAFKA_PORT", kafkaPort.get().toString());
    }
    if (schemaRegistryPort.isPresent()) {
      contents = contents.replaceAll("SCHEMA_REGISTRY_PORT", schemaRegistryPort.get().toString());
    }
    return contents;
  }

  /**
   * Given a resource filename, return the resource file contents as a String.
   *
   * @param resourceFileName    the resource file name
   * @return resource file contents
   * @throws IOException if resource file can't be read
   */
  protected static String getResourceFileContents(
      String resourceFileName
  ) throws IOException {
    // no Kafka / Schema Registry ports
    return getResourceFileContents(resourceFileName, Optional.empty(), Optional.empty());
  }

  /**
   * Utility method to convert a String containing multiple lines into a set of String's where
   * each String is one line. This is useful for creating Flink SQL integration tests based on
   * the tableau results printed via the Table API where the order of results is nondeterministic.
   *
   * @param s multiline String
   * @return set of String's where each member is one line
   */
  protected static Set<String> stringToLineSet(String s) {
    return Sets.newHashSet(Arrays.asList(s.split("\\r?\\n")));
  }

  /**
   * Given a Flink Table API `TableResult` respresenting a SELECT statement result,
   * capture and return the statement's tableau results.
   *
   * @param tableResult Flink Table API `TableResult` respresenting a SELECT statement result
   * @return the SELECT statement's tableau results
   */
  protected static String tableauResults(TableResult tableResult) {
    // capture tableau results printed to stdout in a String
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // The given table result may come from a table backed by the Kafka or Upsert Kafka connector,
    // both of which perform unbounded (neverending) scans. So, in order to prevent tests from blocking
    // on calls to this method, we kick off a thread to kill the underlying job once output has
    // been printed.
    //
    // Note: as of Flink 1.17.0, the Kafka connector will support bounded scanning, which would obviate
    // the need to do this. However, the Upsert Kafka connector will still be unbounded.
    new Thread(() -> {
      while (0 == baos.size()) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          // do nothing; keep waiting
        }
      }
      tableResult.getJobClient().get().cancel();
    }).start();

    try {
      tableResult.print();
    } catch (RuntimeException rte) {
      if (ExceptionUtils.indexOfThrowable(rte, JobCancellationException.class) != -1) {
        // a JobCancellationException in the exception stack is expected due to delayed
        // job cancellation in separate thread; do nothing
      } else {
        rte.printStackTrace();
        System.exit(1);
      }
    }
    System.setOut(System.out);
    return baos.toString();
  }

}
