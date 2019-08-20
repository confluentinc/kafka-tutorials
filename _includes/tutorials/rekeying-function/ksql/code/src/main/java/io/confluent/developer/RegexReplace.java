package io.confluent.developer;

import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;
import io.confluent.ksql.function.udf.UdfParameter;

@UdfDescription(name = "regexReplace", description = "Replace string using a regex")
public class RegexReplace {

  @Udf(description = "regexReplace string using a regex")
  public String regexReplace(
    @UdfParameter(value = "input") final String input,
    @UdfParameter(value = "regex") final String regex,
    @UdfParameter(value = "replacement") final String replacement) {
    return input.replaceAll(regex, replacement);
  }
}