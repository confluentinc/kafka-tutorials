---
seo:
  title: Modernize Messaging Workloads
  description: This ksqlDB recipe shows you how to modernize messaging workloads to move beyond queues and pub/sub
---

# Modernize Messaging Workloads

Traditional messaging systems like Message Queues (MQs), Enterprise Service Buses (ESBs), and Extract, Transform and Load (ETL) tools have been widely used for decades to handle message distribution and inter-service communication across distributed applications.
However, they can no longer keep up with the needs of modern applications across hybrid and multi cloud environments for asynchronicity, heterogeneous datasets and high volume throughput.
Designed as monolithic systems, they are riddled with many challenges: they lack persistence or the ability to efficiently handle highly scalable, efficient and reliable message delivery.
If you instead want to do real-time interactions and in-flight stream processing that modern applications demand, you can use a connector to read critical data from legacy messaging systems into Kafka and then do the processing there.

## Step by step

### Set up your environment

Provision a Kafka cluster in [Confluent Cloud](https://www.confluent.io/confluent-cloud/tryfree/?utm_source=github&utm_medium=ksqldb_recipes&utm_campaign=messaging_modernization).

--8<-- "docs/shared/ccloud_setup.md"

### Read the data in

--8<-- "docs/shared/connect.md"

The concept in this recipe can be applicable to any of the traditional message systems (RabbitMQ, Tibco, IBM MQ, ActiveMQ, etc.), and this specific recipe uses the [RabbitMQ Source Connector for Confluent Cloud](https://docs.confluent.io/cloud/current/connectors/cc-rabbitmq-source.html) which uses the AMQP protocol to communicate with RabbitMQ servers and persists the data in a Kafka topic.

```json
--8<-- "docs/customer-360/messaging-modernization/source.json"
```

--8<-- "docs/shared/manual_insert.md"

### ksqlDB code

Now you can process the data in a variety of ways.

--8<-- "docs/shared/ksqlb_processing_intro.md"

```sql
--8<-- "docs/customer-360/messaging-modernization/process.sql"
```

--8<-- "docs/shared/manual_cue.md"

```sql
--8<-- "docs/customer-360/messaging-modernization/manual.sql"
```

### Cleanup

--8<-- "docs/shared/cleanup.md"
