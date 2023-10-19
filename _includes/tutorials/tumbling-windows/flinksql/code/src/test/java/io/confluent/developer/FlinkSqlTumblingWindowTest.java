package io.confluent.developer;


import org.apache.flink.table.api.TableResult;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class FlinkSqlTumblingWindowTest extends AbstractFlinkKafkaTest {

  @Test
  public void testTumblingWindows() throws Exception {
    // create base ratings table and aggregation table, and populate with test data
    streamTableEnv.executeSql(getResourceFileContents("create-ratings.sql.template",
        Optional.of(kafkaPort), Optional.of(schemaRegistryPort))).await();
    streamTableEnv.executeSql(getResourceFileContents("populate-ratings.sql"));
    streamTableEnv.executeSql(getResourceFileContents("create-ratings-by-6hr-window.sql.template",
        Optional.of(kafkaPort), Optional.of(schemaRegistryPort))).await();
    streamTableEnv.executeSql(getResourceFileContents("populate-ratings-by-6hr-window.sql"));

    TableResult tableResult = streamTableEnv.executeSql(getResourceFileContents("query-ratings-by-6hr-window.sql"));

    // Compare actual and expected results. Convert result output to line sets to compare so that order
    // doesn't matter.
    String actualTableauResults = tableauResults(tableResult);
    String expectedTableauResults = getResourceFileContents("expected-ratings-by-6hr-window.txt");
    assertEquals(stringToLineSet(expectedTableauResults), stringToLineSet(actualTableauResults));
  }

}
