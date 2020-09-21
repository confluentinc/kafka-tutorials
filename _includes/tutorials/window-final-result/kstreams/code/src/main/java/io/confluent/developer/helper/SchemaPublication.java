package io.confluent.developer.helper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import io.confluent.developer.avro.PressureAlert;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

public class SchemaPublication {

    private static final Logger logger = LoggerFactory.getLogger(SchemaPublication.class);

    public static void main(String[] args) {

        Config config = ConfigFactory.load();

        String registryUrl = config.getString("schema.registry.url");

        CachedSchemaRegistryClient schemaRegistryClient  = new CachedSchemaRegistryClient(registryUrl, 10);

        try {
            logger.info(String.format("Schemas publication at: %s", registryUrl));

            schemaRegistryClient.register(
                String.format("%s-value", config.getString("input.topic.name")),
                new AvroSchema(PressureAlert.SCHEMA$)
            );
        } catch (IOException | RestClientException e) {
            e.printStackTrace();
        }
    }
}
