package io.confluent.developer.serialization.serde;

import com.google.gson.Gson;

import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

import io.confluent.developer.avro.Movie;

public class MovieJsonDeserializer implements Deserializer<Movie> {

  private Gson gson = new Gson();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public Movie deserialize(String topic, byte[] data) {

    gson.fromJson(new String(data), Movie.class);
    return null;
  }

  @Override
  public void close() {

  }
}
