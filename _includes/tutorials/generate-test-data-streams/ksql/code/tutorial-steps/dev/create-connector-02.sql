CREATE SOURCE CONNECTOR IF NOT EXISTS NETWORK_TRAFFIC WITH (
    'connector.class'                = 'io.mdrogalis.voluble.VolubleSourceConnector',
    'key.converter'                  = 'org.apache.kafka.connect.storage.StringConverter',

    'genkp.devices.with'                   = '#{Internet.macAddress}',
     'genv.devices.name.with'              = '#{GameOfThrones.dragon}',
     'genv.devices.location->city.with'    = '#{Address.city}',
     'genv.devices.location->country.with' = '#{Address.country}',
    'topic.devices.records.exactly'        = 10,

     'genkp.traffic.with'                = '#{Number.randomDigit}',
    'attrkp.traffic.null.rate'           = 1,
      'genv.traffic.mac.matching'        = 'devices.key',
      'genv.traffic.bytes_sent.with'     = '#{Number.numberBetween ''64'',''4096''}',
     'topic.traffic.throttle.ms'         = 500 
);
