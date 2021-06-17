CREATE SOURCE CONNECTOR CLICKS WITH (
    'connector.class'             = 'io.mdrogalis.voluble.VolubleSourceConnector',
    'key.converter'               = 'org.apache.kafka.connect.storage.StringConverter',
    
    'genkp.clicks.with'           = '#{Number.randomDigit}',
    'attrkp.clicks.null.rate'     = 1,
    'genv.clicks.source_ip.with'  = '#{Internet.ipV4Address}',
    'genv.clicks.host.with'       = '#{Internet.url}',
    'genv.clicks.path.with'       = '#{File.fileName}',
    'genv.clicks.user_agent.with' = '#{Internet.userAgentAny}',
    'topic.clicks.throttle.ms'    = 1000 
);
