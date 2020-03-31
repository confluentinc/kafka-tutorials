CREATE SOURCE CONNECTOR DATAGENERATOR_01 WITH (
    'connector.class'                = 'io.mdrogalis.voluble.VolubleSourceConnector',
    'key.converter'                  = 'org.apache.kafka.connect.storage.StringConverter',

    'genkp.customers.with'                 = '#{Internet.uuid}',
    'genv.customers.name.with'             = '#{HitchhikersGuideToTheGalaxy.character}',
    'genv.customers.email.with'            = '#{Internet.emailAddress}',
    'genv.customers.location->city.with'   = '#{HitchhikersGuideToTheGalaxy.location}',
    'genv.customers.location->planet.with' = '#{HitchhikersGuideToTheGalaxy.planet}',
    'topic.customers.records.exactly'      = 10,

    'genkp.transactions.with'                = '#{Internet.uuid}',
    'genv.transactions.customer_id.matching' = 'customers.key',
    'genv.transactions.cost.with'            = '#{Commerce.price}',
    'genv.transactions.card_type.with'       = '#{Business.creditCardType}',
    'genv.transactions.item.with'            = '#{Beer.name}',
    'topic.transactions.throttle.ms'         = 500 
);