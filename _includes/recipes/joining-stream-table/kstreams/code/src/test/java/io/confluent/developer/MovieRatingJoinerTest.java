package io.confluent.developer;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RatedMovie;
import io.confluent.developer.avro.Rating;
import org.junit.Test;

import static org.junit.Assert.*;

public class MovieRatingJoinerTest {

    @Test
    public void apply() {
        RatedMovie actualRatedMovie;

        Movie treeOfLife = Movie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).build();
        Rating rating = Rating.newBuilder().setId(354).setRating(9.8).build();
        RatedMovie expectedRatedMovie = RatedMovie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setRating(9.8).build();

        MovieRatingJoiner joiner = new MovieRatingJoiner();
        actualRatedMovie = joiner.apply(rating, treeOfLife);

        assertEquals(actualRatedMovie, expectedRatedMovie);
    }
}