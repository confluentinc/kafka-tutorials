package io.confluent.developer;

import io.confluent.developer.avro.LoginEvent;
import io.confluent.developer.avro.LoginRollup;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.kstream.Aggregator;

public class LoginAggregator implements Aggregator<String, LoginEvent, LoginRollup> {
  // Adding a change here to trigger a build ++++
  // ------++++++++++++
  @Override
  public LoginRollup apply(final String appId,
                           final LoginEvent loginEvent,
                           final LoginRollup loginRollup) {
    final String userId = loginEvent.getUserId();
    final Map<String, Map<String, Long>> allLogins = loginRollup.getLoginByAppAndUser();
    final Map<String, Long> userLogins = allLogins.computeIfAbsent(appId, key -> new HashMap<>());
    userLogins.compute(userId, (k, v) -> v == null ? 1L : v + 1L);
    return loginRollup;
  }
}
