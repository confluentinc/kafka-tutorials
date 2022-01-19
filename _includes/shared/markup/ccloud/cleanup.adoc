To clean up the ksqlDB resources created by this recipe, use the ksqlDB commands shown below (substitute stream or topic name, as appropriate).
By including the `DELETE TOPIC` clause, the topic backing the stream or table is also deleted, asynchronously.

```
DROP STREAM IF EXISTS <stream_name> DELETE TOPIC;
DROP TABLE IF EXISTS <table_name> DELETE TOPIC;
```

If you also created connectors, you'll need to remove those as well.
