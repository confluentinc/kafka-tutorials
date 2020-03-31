CREATE SOURCE CONNECTOR JDBC_SOURCE_POSTGRES_01 WITH (
    'connector.class'= 'io.confluent.connect.jdbc.JdbcSourceConnector',
    'connection.url'= 'jdbc:postgresql://postgres:5432/postgres',
    'connection.user'= 'postgres',
    'connection.password'= 'postgres',
    'mode'= 'incrementing',
    'incrementing.column.name'= 'city_id',
    'topic.prefix'= 'postgres_',
    'transforms'= 'copyFieldToKey,extractValuefromStruct',
    'transforms.copyFieldToKey.type'= 'org.apache.kafka.connect.transforms.ValueToKey',
    'transforms.copyFieldToKey.fields'= 'city_id',
    'transforms.extractValuefromStruct.type'= 'org.apache.kafka.connect.transforms.ExtractField$Key',
    'transforms.extractValuefromStruct.field'= 'city_id',
    'key.converter' = 'org.apache.kafka.connect.converters.IntegerConverter'
);