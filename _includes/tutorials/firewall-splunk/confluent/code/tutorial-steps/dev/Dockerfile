FROM confluentinc/cp-server-connect-base:7.0.1

ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-splunk-s2s:1.0.5
