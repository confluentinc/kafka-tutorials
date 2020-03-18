CREATE STREAM raw_quotes(ticker varchar, bid int, ask int, bidqty int, askqty int)
    WITH (kafka_topic='stockquotes', value_format='avro', key='ticker', partitions=1);

CREATE STREAM vwap WITH (kafka_topic = 'vwap', partitions = 1) AS
    SELECT ticker,
           vwap(bid, bidqty, ask, askqty) AS vwap
    FROM raw_quotes;