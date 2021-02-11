curl -X PUT http://localhost:8083/connectors/network_traffic/config \
     -i -H "Content-Type: application/json" -d '{
    "connector.class"                : "io.mdrogalis.voluble.VolubleSourceConnector",
    "key.converter"                  : "org.apache.kafka.connect.storage.StringConverter",
    "genkp.devices.with"                   : "#{Internet.macAddress}",
     "genv.devices.name.with"              : "#{GameOfThrones.dragon}",
     "genv.devices.location->city.with"    : "#{Address.city}",
     "genv.devices.location->country.with" : "#{Address.country}",
    "topic.devices.records.exactly"        : 10,
     "genkp.traffic.with"                : "#{Number.randomDigit}",
    "attrkp.traffic.null.rate"           : 1,
      "genv.traffic.mac.matching"        : "devices.key",
      "genv.traffic.bytes_sent.with"     : "#{Number.numberBetween \u002764\u0027,\u00274096\u0027}",
     "topic.traffic.throttle.ms"         : 500 
}'
