CREATE SINK CONNECTOR JDBC_SINK_POSTGRES_01 WITH (
    'connector.class'= 'io.confluent.connect.jdbc.JdbcSourceConnector',
    'connection.url'= 'jdbc:postgresql://postgres:5432/postgres',
    'connection.user'= 'postgres',
    'connection.password'= 'postgres',
    'topics'= 'deviceEvents',
    'key.converter'       = 'org.apache.kafka.connect.storage.StringConverter',
    'auto.create'         = 'true',
    'auto.evolve'         = 'true',
    'transforms.setTimestampType.field'= 'eventTime',
);

