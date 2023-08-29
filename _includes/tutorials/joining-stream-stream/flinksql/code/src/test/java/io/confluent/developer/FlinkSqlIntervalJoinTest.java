package io.confluent.developer;


import org.apache.flink.table.api.TableResult;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class FlinkSqlIntervalJoinTest extends AbstractFlinkKafkaTest {

  @Test
  public void simpleSelect() throws Exception {
    // create base movie sales table and aggregation table, and populate with test data
    streamTableEnv.executeSql(getResourceFileContents("create-orders.sql.template",
        Optional.of(kafkaPort),Optional.of(schemaRegistryPort))).await();
    streamTableEnv.executeSql(getResourceFileContents("create-shipments.sql.template",
        Optional.of(kafkaPort),Optional.of(schemaRegistryPort))).await();
    streamTableEnv.executeSql(getResourceFileContents("populate-orders.sql"));
    streamTableEnv.executeSql(getResourceFileContents("populate-shipments.sql"));
    streamTableEnv.executeSql(getResourceFileContents("create-shipped-orders.sql.template",
        Optional.of(kafkaPort),Optional.of(schemaRegistryPort))).await();

    // In Flink 17 and later
    // by setting 'scan.bounded.mode' = 'latest-offset' the CREATE TABLE statement, will
    // cause this INSERT to terminate once the latest offset is reached.
    streamTableEnv.executeSql(getResourceFileContents("populate-shipped-orders-table.sql"));

    // execute query on result table that should have joined shipments with orders
    TableResult tableResult = streamTableEnv.executeSql(getResourceFileContents("query-join-order-shipments.sql"));

    // Compare actual and expected results. Convert result output to line sets to compare so that order
    // doesn't matter
    String actualTableauResults = tableauResults(tableResult);
    String expectedTableauResults = getResourceFileContents("expected-shipped-orders.txt");
    assertEquals(stringToLineSet(actualTableauResults), stringToLineSet(expectedTableauResults));
  }

}
