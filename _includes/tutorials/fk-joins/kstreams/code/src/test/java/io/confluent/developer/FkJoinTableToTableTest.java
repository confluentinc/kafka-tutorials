package io.confluent.developer;

import io.confluent.developer.avro.Album;
import io.confluent.developer.avro.MusicInterest;
import io.confluent.developer.avro.TrackPurchase;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class FkJoinTableToTableTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    private static <T extends SpecificRecord> SpecificAvroSerializer<T> getAvroSerializer(Properties envProps) {
        final SpecificAvroSerializer<T> serializer = new SpecificAvroSerializer<>();

        final Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        serializer.configure(config, false);

        return serializer;
    }

    private static <T extends SpecificRecord> SpecificAvroDeserializer<T> getAvroDeserializer(Properties envProps) {
        final SpecificAvroDeserializer<T> deserializer = new SpecificAvroDeserializer<>();

        final Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", envProps.getProperty("schema.registry.url"));
        deserializer.configure(config, false);

        return deserializer;
    }

    @Test
    public void testJoin() throws IOException {
        final FkJoinTableToTable fkJoin = new FkJoinTableToTable();
        final Properties envProps = fkJoin.loadEnvProperties(TEST_CONFIG_FILE);
        final Properties streamProps = fkJoin.buildStreamsProperties(envProps);

        final String albumInputTopic = envProps.getProperty("album.topic.name");
        final String userPurchaseTopic = envProps.getProperty("user.tracks.purchase.topic.name");
        final String joinedResultOutputTopic = envProps.getProperty("music.interest.topic.name");

        final Topology topology = fkJoin.buildTopology(envProps);
        try (final TopologyTestDriver testDriver = new TopologyTestDriver(topology, streamProps)) {

            final Serializer<Long> keySerializer = Serdes.Long().serializer();
            final Serializer<Album> albumSerializer = getAvroSerializer(envProps);
            final SpecificAvroSerializer<TrackPurchase> trackPurchaseSerializer = getAvroSerializer(envProps);

            final SpecificAvroDeserializer<MusicInterest> musicInterestDeserializer = getAvroDeserializer(envProps);

            final TestInputTopic<Long, Album>  albumTestInputTopic = testDriver.createInputTopic(albumInputTopic,keySerializer, albumSerializer);
            final TestInputTopic<Long, TrackPurchase> trackPurchaseInputTopic = testDriver.createInputTopic(userPurchaseTopic, keySerializer, trackPurchaseSerializer);
            final TestOutputTopic<String, MusicInterest> outputTopic = testDriver.createOutputTopic(joinedResultOutputTopic, new StringDeserializer(), musicInterestDeserializer);


            final List<Album> albums = new ArrayList<>();
            albums.add(Album.newBuilder().setId(5L).setTitle("Physical Graffiti").setArtist("Led Zeppelin").setGenre("Rock").build());
            albums.add(Album.newBuilder().setId(6L).setTitle("Highway to Hell").setArtist("AC/DC").setGenre("Rock").build());
            albums.add(Album.newBuilder().setId(7L).setTitle("Radio").setArtist("LL Cool J").setGenre("Hip hop").build());
            albums.add(Album.newBuilder().setId(8L).setTitle("King of Rock").setArtist("Run-D.M.C").setGenre("Rap rock").build());

            final List<TrackPurchase> trackPurchases = new ArrayList<>();
            trackPurchases.add(TrackPurchase.newBuilder().setId(100).setAlbumId(5L).setSongTitle("Houses Of The Holy").setPrice(0.99).build());
            trackPurchases.add(TrackPurchase.newBuilder().setId(101).setAlbumId(8L).setSongTitle("King Of Rock").setPrice(0.99).build());
            trackPurchases.add(TrackPurchase.newBuilder().setId(102).setAlbumId(6L).setSongTitle("Shot Down In Flames").setPrice(0.99).build());
            trackPurchases.add(TrackPurchase.newBuilder().setId(103).setAlbumId(7L).setSongTitle("Rock The Bells").setPrice(0.99).build());
            trackPurchases.add(TrackPurchase.newBuilder().setId(104).setAlbumId(8L).setSongTitle("Can You Rock It Like This").setPrice(0.99).build());
            trackPurchases.add(TrackPurchase.newBuilder().setId(105).setAlbumId(6L).setSongTitle("Highway To Hell").setPrice(0.99).build());
            trackPurchases.add(TrackPurchase.newBuilder().setId(106).setAlbumId(5L).setSongTitle("Kashmir").setPrice(0.99).build());

            final List<MusicInterest> expectedMusicInterestJoinResults = new ArrayList<>();
            expectedMusicInterestJoinResults.add(MusicInterest.newBuilder().setId("5-100").setGenre("Rock").setArtist("Led Zeppelin").build());
            expectedMusicInterestJoinResults.add(MusicInterest.newBuilder().setId("8-101").setGenre("Rap rock").setArtist("Run-D.M.C").build());
            expectedMusicInterestJoinResults.add(MusicInterest.newBuilder().setId("6-102").setGenre("Rock").setArtist("AC/DC").build());
            expectedMusicInterestJoinResults.add(MusicInterest.newBuilder().setId("7-103").setGenre("Hip hop").setArtist("LL Cool J").build());
            expectedMusicInterestJoinResults.add(MusicInterest.newBuilder().setId("8-104").setGenre("Rap rock").setArtist("Run-D.M.C").build());
            expectedMusicInterestJoinResults.add(MusicInterest.newBuilder().setId("6-105").setGenre("Rock").setArtist("AC/DC").build());
            expectedMusicInterestJoinResults.add(MusicInterest.newBuilder().setId("5-106").setGenre("Rock").setArtist("Led Zeppelin").build());

            for (final Album album : albums) {
                albumTestInputTopic.pipeInput(album.getId(), album);
            }

            for (final TrackPurchase trackPurchase : trackPurchases) {
                trackPurchaseInputTopic.pipeInput(trackPurchase.getId(), trackPurchase);
            }

            final List<MusicInterest> actualJoinResults = outputTopic.readValuesToList();

            assertEquals(expectedMusicInterestJoinResults, actualJoinResults);
        }
    }
}