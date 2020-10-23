CREATE TABLE suspicious_names (CREATED_DATE VARCHAR,
                               COMPANY_NAME VARCHAR PRIMARY KEY,
                               COMPANY_ID INT)
    WITH (kafka_topic='suspicious_names',
          partitions=1,
          value_format='JSON',
          timestamp='CREATED_DATE',
          timestamp_format='yyyy-MM-dd HH:mm:ss');

INSERT INTO suspicious_names (CREATED_DATE, COMPANY_NAME, COMPANY_ID) VALUES ('2019-03-08 00:00:00', 'Verizon', 1);
INSERT INTO suspicious_names (CREATED_DATE, COMPANY_NAME, COMPANY_ID) VALUES ('2019-10-31 00:00:00', 'Spirit Halloween', 2);
INSERT INTO suspicious_names (CREATED_DATE, COMPANY_NAME, COMPANY_ID) VALUES ('2019-12-15 00:00:00', 'Best Buy', 3);

CREATE STREAM transactions (TXN_ID BIGINT, USERNAME VARCHAR, RECIPIENT VARCHAR, AMOUNT DOUBLE, TIMESTAMP VARCHAR)
    WITH (kafka_topic='transactions',
          partitions=1,
          value_format='JSON',
          timestamp='TIMESTAMP',
          timestamp_format='yyyy-MM-dd HH:mm:ss');

INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (9900, 'Abby Normal', 'Verizon', 22.0, '2020-10-20 13:05:36');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (12, 'Victor von Frankenstein', 'Tattered Cover', 7.0, '2020-10-20 13:07:59');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (13, 'Frau Blücher', 'Peebles', 70.0, '2020-10-20 13:15:00');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (9903, 'Abby Normal', 'Verizon', 61.0, '2020-10-20 13:31:02');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (9901, 'Abby Normal', 'Spirit Halloween', 83.0, '2020-10-20 13:44:41');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (9902, 'Abby Normal', 'Spirit Halloween', 46.0, '2020-10-20 13:44:43');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (9904, 'Abby Normal', 'Spirit Halloween', 59.0, '2020-10-20 13:44:44');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (6, 'Victor von Frankenstein', 'Confluent Cloud', 21.0, '2020-10-20 13:47:51');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (18, 'Frau Blücher', 'Target', 70.0, '2020-10-20 13:52:01');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (7, 'Victor von Frankenstein', 'Verizon', 100.0, '2020-10-20 13:55:06');
INSERT INTO transactions (TXN_ID, USERNAME, RECIPIENT, AMOUNT, TIMESTAMP) VALUES (19, 'Frau Blücher', 'Goodwill', 7.0, '2020-10-20 14:12:32');

CREATE STREAM suspicious_transactions
    WITH (kafka_topic='suspicious_transactions', partitions=1, value_format='JSON') AS
    SELECT T.TXN_ID, T.USERNAME, T.RECIPIENT, T.AMOUNT, T.TIMESTAMP
    FROM transactions T
    INNER JOIN
    suspicious_names S
    ON T.RECIPIENT = S.COMPANY_NAME;

CREATE TABLE accounts_to_monitor
    WITH (kafka_topic='accounts_to_monitor', partitions=1, value_format='JSON') AS
    SELECT TIMESTAMPTOSTRING(WINDOWSTART, 'yyyy-MM-dd HH:mm:ss Z') AS WINDOW_START,
           TIMESTAMPTOSTRING(WINDOWEND, 'yyyy-MM-dd HH:mm:ss Z') AS WINDOW_END,
           USERNAME
    FROM suspicious_transactions
    WINDOW TUMBLING (SIZE 24 HOURS)
    GROUP BY USERNAME
    HAVING COUNT(*) > 3;