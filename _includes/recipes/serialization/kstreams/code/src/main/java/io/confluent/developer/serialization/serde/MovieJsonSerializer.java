package io.confluent.developer.serialization.serde;


import com.google.gson.Gson;

import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.Charset;
import java.util.Map;

import io.confluent.developer.avro.Movie;

public class MovieJsonSerializer implements Serializer<Movie> {

  private Gson gson = new Gson();

  @Override
  public void configure(Map<String, ?> map, boolean b) {

  }

  @Override
  public byte[] serialize(String topic, Movie data) {
    return gson.toJson(data).getBytes(Charset.forName("UTF-8"));
  }

  @Override
  public void close() {
  }


}