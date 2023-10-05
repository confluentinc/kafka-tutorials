package io.confluent.developer;


import org.apache.flink.table.api.TableResult;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class FlinkSqlTopNTest extends AbstractFlinkKafkaTest {

  @Test
  public void simpleSelect() throws Exception {
    // create base movie sales table and aggregation table, and populate with test data
    streamTableEnv.executeSql(getResourceFileContents("create-clicks-table.sql.template",
        Optional.of(kafkaPort),Optional.of(schemaRegistryPort))).await();
    streamTableEnv.executeSql(getResourceFileContents("create-deduplicated-clicks-table.sql.template",
        Optional.of(kafkaPort),Optional.of(schemaRegistryPort))).await();
    streamTableEnv.executeSql(getResourceFileContents("populate-clicks.sql"));

    // In Flink 17 and later
    // by setting 'scan.bounded.mode' = 'latest-offset' the CREATE TABLE statement, will
    // cause this INSERT to terminate once the latest offset is reached.
    streamTableEnv.executeSql(getResourceFileContents("populate-deduplicated-clicks-table.sql"));

    // execute query on result table that should have joined shipments with orders
    TableResult tableResult = streamTableEnv.executeSql(getResourceFileContents("query-deduplicated-clicks.sql"));

    // Compare actual and expected results. Convert result output to line sets to compare so that order
    // doesn't matter
    String actualTableauResults = tableauResults(tableResult);
    String expectedTableauResults = getResourceFileContents("expected-deduplicated-clicks.txt");
    assertEquals(stringToLineSet(actualTableauResults), stringToLineSet(expectedTableauResults));
  }

}
