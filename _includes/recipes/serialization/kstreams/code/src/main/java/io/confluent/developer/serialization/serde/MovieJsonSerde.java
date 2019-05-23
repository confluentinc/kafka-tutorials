package io.confluent.developer.serialization.serde;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.confluent.developer.avro.Movie;

public class MovieJsonSerde extends Serdes.WrapperSerde<Movie> {

  private MovieJsonSerde() {
    super(new MovieJsonSerializer(), new MovieJsonDeserializer());
  }

  static public Serde<Movie> newMovieJsonSerde() {
    return new MovieJsonSerde();
  }

}
