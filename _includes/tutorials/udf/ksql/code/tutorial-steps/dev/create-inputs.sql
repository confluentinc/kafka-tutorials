CREATE STREAM raw_quotes(ticker varchar, bid int, ask int, bidqty int, askqty int)
    WITH (kafka_topic='stockquotes', value_format='avro', key='ticker', partitions=1);