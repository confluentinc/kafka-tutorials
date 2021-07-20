# docker run -v $PWD/configuration/prod.properties:/prod.properties io.confluent.developer/aggregating-average:0.0.1

# run with network
docker run -v $PWD/configuration/prod.properties:/prod.properties --network cp_network io.confluent.developer/aggregating-average:0.0.1