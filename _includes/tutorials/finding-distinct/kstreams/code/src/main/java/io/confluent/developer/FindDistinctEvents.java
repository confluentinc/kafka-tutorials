package io.confluent.developer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import io.confluent.common.utils.TestUtils;
import io.confluent.developer.avro.Click;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class FindDistinctEvents {

    private static final String storeName = "eventId-store";

    /**
     * Discards duplicate click events from the input stream by ip address
     * <p>
     * Duplicate records are detected based on ip address
     * The transformer remembers known ip addresses within an associated window state
     * store, which automatically purges/expires IPs from the store after a certain amount of
     * time has passed to prevent the store from growing indefinitely.
     * <p>
     * Note: This code is for demonstration purposes and was not tested for production usage.
     */
    private static class DeduplicationProcessorSupplier<K, V, E> implements FixedKeyProcessorSupplier<K, V, V> {

        /**
         * Key: ip address
         * Value: timestamp (event-time) of the corresponding event when the event ID was seen for the
         * first time
         */
        private WindowStore<E, Long> eventIdStore;

        private final Duration windowSize;
        private final long leftDurationMs;
        private final long rightDurationMs;

        private final KeyValueMapper<K, V, E> idExtractor;

        /**
         * @param idExtractor                  extracts a unique identifier from a record by which we de-duplicate input
         *                                     records; if it returns null, the record will not be considered for
         *                                     de-duping but forwarded as-is.
         */
        DeduplicationProcessorSupplier(final KeyValueMapper<K, V, E> idExtractor) {
            // How long we "remember" an event.  During this time, any incoming duplicates of the event
            // will be, well, dropped, thereby de-duplicating the input data.
            //
            // The actual value depends on your use case.  To reduce memory and disk usage, you could
            // decrease the size to purge old windows more frequently at the cost of potentially missing out
            // on de-duplicating late-arriving records.
            windowSize = Duration.ofMinutes(2);

            final long maintainDurationPerEventInMs = windowSize.toMillis();
            leftDurationMs = maintainDurationPerEventInMs / 2;
            rightDurationMs = maintainDurationPerEventInMs - leftDurationMs;
            this.idExtractor = idExtractor;
        }

        @Override
        public Set<StoreBuilder<?>> stores() {

            // retention period must be at least window size -- for this use case, we don't need a longer retention period
            // and thus just use the window size as retention time
            final Duration retentionPeriod = windowSize;

            final StoreBuilder<WindowStore<String, Long>> dedupStoreBuilder = Stores.windowStoreBuilder(
                Stores.persistentWindowStore(storeName,
                    retentionPeriod,
                    windowSize,
                    false
                ),
                Serdes.String(),
                Serdes.Long());

            return Collections.singleton(dedupStoreBuilder);
        }

        @Override
        public FixedKeyProcessor<K, V, V> get() {
            return new DeduplicationProcessor();
        }

        private class DeduplicationProcessor extends ContextualFixedKeyProcessor<K, V, V> {

            @Override
            public void init(final FixedKeyProcessorContext<K, V> context) {
                super.init(context);
                eventIdStore = context().getStateStore(storeName);
            }

            @Override
            public void process(final FixedKeyRecord<K, V> record) {
                final E eventId = idExtractor.apply(record.key(), record.value());
                if (eventId == null) {
                    context().forward(record);
                } else {
                    if (isDuplicate(eventId, record.timestamp())) {
                        updateTimestampOfExistingEventToPreventExpiry(eventId, record.timestamp());
                    } else {
                        rememberNewEvent(eventId, record.timestamp());
                        context().forward(record);
                    }
                }
            }

            private boolean isDuplicate(final E eventId, long eventTime) {
                final WindowStoreIterator<Long> timeIterator = eventIdStore.fetch(
                    eventId,
                    eventTime - leftDurationMs,
                    eventTime + rightDurationMs);
                final boolean isDuplicate = timeIterator.hasNext();
                timeIterator.close();
                return isDuplicate;
            }

            private void updateTimestampOfExistingEventToPreventExpiry(final E eventId, final long newTimestamp) {
                eventIdStore.put(eventId, newTimestamp, newTimestamp);
            }

            private void rememberNewEvent(final E eventId, final long timestamp) {
                eventIdStore.put(eventId, timestamp, timestamp);
            }
        }
    }

    private SpecificAvroSerde<Click> buildClicksSerde(final Properties allProps) {
        final SpecificAvroSerde<Click> serde = new SpecificAvroSerde<>();
        final Map<String, String> config = (Map)allProps;
        serde.configure(config, false);
        return serde;
    }

    public Topology buildTopology(Properties allProps,
                                  final SpecificAvroSerde<Click> clicksSerde) {
        final StreamsBuilder builder = new StreamsBuilder();

        final String inputTopic = allProps.getProperty("input.topic.name");
        final String outputTopic = allProps.getProperty("output.topic.name");

        builder
            .stream(inputTopic, Consumed.with(Serdes.String(), clicksSerde))
            .processValues(new DeduplicationProcessorSupplier<>((key, value) -> value.getIp()), storeName)
            .filter((k, v) -> v != null)
            .to(outputTopic, Produced.with(Serdes.String(), clicksSerde));

        return builder.build();
    }

    public void createTopics(Properties allProps) {
        AdminClient client = AdminClient.create(allProps);

        List<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic(
            allProps.getProperty("input.topic.name"),
            Integer.parseInt(allProps.getProperty("input.topic.partitions")),
            Short.parseShort(allProps.getProperty("input.topic.replication.factor"))));
        topics.add(new NewTopic(
            allProps.getProperty("output.topic.name"),
            Integer.parseInt(allProps.getProperty("output.topic.partitions")),
            Short.parseShort(allProps.getProperty("output.topic.replication.factor"))));

        client.createTopics(topics);
        client.close();
    }

    public static Properties loadEnvProperties(String fileName) throws IOException {
        Properties allProps = new Properties();
        FileInputStream input = new FileInputStream(fileName);
        allProps.load(input);
        input.close();

        return allProps;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                "This program takes one argument: the path to an environment configuration file.");
        }

        new FindDistinctEvents().runRecipe(args[0]);
    }

    private void runRecipe(final String configPath) throws IOException {
        final Properties allProps = new Properties();
        try (InputStream inputStream = new FileInputStream(configPath)) {
            allProps.load(inputStream);
        }
        allProps.put(StreamsConfig.APPLICATION_ID_CONFIG, allProps.getProperty("application.id"));
        allProps.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());

        final Topology topology = this.buildTopology(allProps, this.buildClicksSerde(allProps));

        this.createTopics(allProps);

        final KafkaStreams streams = new KafkaStreams(topology, allProps);
        final CountDownLatch latch = new CountDownLatch(1);

        // Attach shutdown handler to catch Control-C.
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close(Duration.ofSeconds(5));
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);

    }
}
