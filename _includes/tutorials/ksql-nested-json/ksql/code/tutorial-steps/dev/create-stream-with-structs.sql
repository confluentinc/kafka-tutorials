CREATE STREAM financial_report (
	      id VARCHAR,
              transaction STRUCT<num_shares INT,
             	                  amount DOUBLE,
             	                  txn_ts VARCHAR,
             	                  customer STRUCT<first_name VARCHAR, 
             	                                  last_name VARCHAR, 
             	                                  id INT, 
             	                                  email VARCHAR>,
                                   company STRUCT<name VARCHAR, 
                                                  ticker VARCHAR, 
                                                  id VARCHAR, 
                                                  address VARCHAR>>)     
 WITH (KAFKA_TOPIC='fin_txns',
       VALUE_FORMAT='JSON',
       TIMESTAMP='txn_ts',
       TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX',
       PARTITIONS=1);