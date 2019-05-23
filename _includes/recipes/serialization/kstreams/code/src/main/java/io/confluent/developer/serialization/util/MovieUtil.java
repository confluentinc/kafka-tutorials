package io.confluent.developer.serialization.util;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import io.confluent.developer.avro.Movie;

public interface MovieUtil {

  static Movie parseMovie(String text) {
    String[] tokens = text.split("::");
    String id = tokens[0];
    String title = tokens[1];
    String releaseYear = tokens[2];
    String country = tokens[4];
    String genres = tokens[7];
    String actors = tokens[8];
    String directors = tokens[9];
    String composers = tokens[10];
    String screenwriters = tokens[11];
    String cinematographer = tokens[12];
    String productionCompanies = "";
    if (tokens.length > 13) {
      productionCompanies = tokens[13];
    }

    Movie movie = new Movie();
    movie.setMovieId(Long.parseLong(id));
    movie.setTitle(title);
    movie.setReleaseYear(Integer.parseInt(releaseYear));
    movie.setCountry(country);
    movie.setGenres(MovieUtil.parseArray(genres));
    movie.setActors(MovieUtil.parseArray(actors));
    movie.setDirectors(MovieUtil.parseArray(directors));
    movie.setComposers(MovieUtil.parseArray(composers));
    movie.setScreenwriters(MovieUtil.parseArray(screenwriters));
    movie.setCinematographer(cinematographer);
    movie.setProductionCompanies(MovieUtil.parseArray(productionCompanies));

    return movie;
  }

  static List<String> parseArray(String text) {
    return Collections.list(new StringTokenizer(text, "|")).stream()
        .map(token -> (String) token)
        .collect(Collectors.toList());
  }
}
