package io.confluent.developer;

import io.confluent.developer.avro.Movie;

import io.confluent.developer.avro.RawMovie;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class TransformEventsTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";
    private TransformationEngine transEngine;

    @Test
    public void checkIfYearFieldEndsUpSplitted() throws IOException {

        KafkaProducer<String, Movie> movieProducer;
        KafkaProducer<String, RawMovie> rawMovieProducer;
        KafkaConsumer<String, RawMovie> rawMovieConsumer;
        KafkaConsumer<String, Movie> outputConsumer;

        TransformEvents te = new TransformEvents();
        Properties envProps = te.loadEnvProperties(TEST_CONFIG_FILE);
        te.createTopics(envProps);

        Properties producerProps = te.buildProducerProperties(envProps);
        Properties inputConsumerProps = te.buildConsumerProperties("inputGroup", envProps);
        Properties outputConsumerProps = te.buildConsumerProperties("outputGroup", envProps);

        String inputTopic = envProps.getProperty("input.topic.name");
        String outputTopic = envProps.getProperty("output.topic.name");

        List<RawMovie> input = new ArrayList<>();
        input.add(RawMovie.newBuilder().setId(294).setTitle("Die Hard::1988").setGenre("action").build());
        input.add(RawMovie.newBuilder().setId(354).setTitle("Tree of Life::2011").setGenre("drama").build());
        input.add(RawMovie.newBuilder().setId(782).setTitle("A Walk in the Clouds::1995").setGenre("romance").build());
        input.add(RawMovie.newBuilder().setId(128).setTitle("The Big Lebowski::1998").setGenre("comedy").build());

        List<Movie> expectedOutput = new ArrayList<>();
        expectedOutput.add(Movie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setGenre("action").build());
        expectedOutput.add(Movie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setGenre("drama").build());
        expectedOutput.add(Movie.newBuilder().setTitle("A Walk in the Clouds").setId(782).setReleaseYear(1995).setGenre("romance").build());
        expectedOutput.add(Movie.newBuilder().setTitle("The Big Lebowski").setId(128).setReleaseYear(1998).setGenre("comedy").build());

        rawMovieProducer = te.createRawMovieProducer(producerProps);
        movieProducer = te.createMovieProducer(producerProps);
        rawMovieConsumer = te.createRawMovieConsumer(inputConsumerProps);

        // Start the transformation engine, which will perform the transformation
        // processing in a background thread using Kafka's consumer API.
        transEngine = new TransformationEngine(inputTopic, outputTopic,
            rawMovieConsumer, movieProducer);

        Thread transEngineThread = new Thread(transEngine);
        List<Movie> actualOutput = null;

        try {

            transEngineThread.start();
            // Produce the raw movies for the testing process...
            produceRawMovies(inputTopic, input, rawMovieProducer);

            // Read the transformed records from the output topic,
            // that has been put there by the transformation engine.
            outputConsumer = te.createMovieConsumer(outputConsumerProps);
            actualOutput = consumeMovies(outputTopic, outputConsumer);

        } finally {
            transEngine.shutdown();
        }
        
        Assert.assertEquals(expectedOutput, actualOutput);
        
    }

    @After
    public void tearDown() throws IOException {

        transEngine.shutdown();
        TransformEvents te = new TransformEvents();
        Properties envProps = te.loadEnvProperties(TEST_CONFIG_FILE);
        te.deleteTopics(envProps);

    }

    private List<Movie> consumeMovies(String outputTopic,
                                        KafkaConsumer<String, Movie> consumer) {

        // Wait 2 seconds until all records are fully persisted,
        // to avoid a race condition between producers and consumers...
        try { Thread.sleep(2000); } catch (Exception ex) {}
        
        List<Movie> output = new ArrayList<Movie>();
        consumer.subscribe(Arrays.asList(outputTopic));
        ConsumerRecords<String, Movie> records = consumer.poll(5000);

        for (ConsumerRecord<String, Movie> record : records) {
            output.add(record.value());
        }

        return output;

    }

    private void produceRawMovies(String inputTopic, List<RawMovie> rawMovies,
                                 KafkaProducer<String, RawMovie> producer) {

        ProducerRecord<String, RawMovie> record = null;
        for (RawMovie movie : rawMovies) {
            record = new ProducerRecord<String, RawMovie>(inputTopic, movie);
            producer.send(record);
        }

    }

}