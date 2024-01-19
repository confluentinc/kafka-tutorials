package io.confluent.developer;

import com.typesafe.config.Config;
import io.confluent.developer.avro.PressureAlert;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class PressureDatetimeExtractor implements TimestampExtractor {

    private final DateTimeFormatter formatter;
    private static final Logger logger = LoggerFactory.getLogger(TimestampExtractor.class);

    public PressureDatetimeExtractor(Config config) {
        this.formatter = DateTimeFormatter.ofPattern(config.getString("sensor.datetime.pattern"));
    }

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        try {
            String dateTimeString = ((PressureAlert) record.value()).getDatetime();
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, this.formatter);
            return zonedDateTime.toInstant().toEpochMilli();
        } catch (ClassCastException cce) {
            logger.error("failed to cast the PressureAlert: ", cce);
        } catch (DateTimeParseException dtpe) {
            logger.error("fail to parse the event datetime due to: ", dtpe);
        }

        // Returning a negative number will cause records to be skipped
        return -1L;
    }
}
