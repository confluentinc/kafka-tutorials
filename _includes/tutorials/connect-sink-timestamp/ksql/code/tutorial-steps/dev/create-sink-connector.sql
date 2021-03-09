CREATE SINK CONNECTOR JDBC_SINK_POSTGRES_01 WITH (
    'connector.class' = 'io.confluent.connect.jdbc.JdbcSourceConnector',
    'connection.url' = 'jdbc:postgresql://postgres:5432/postgres',
    'connection.user' = 'postgres',
    'connection.password' = 'postgres',
    'topics' = 'deviceEvents',
    'auto.create' = 'true',
    'auto.evolve' = 'true',
    'topic.prefix' = '',
    'mode' = 'bulk',
    'transforms.setTimestampType.field' = 'eventTime'
);