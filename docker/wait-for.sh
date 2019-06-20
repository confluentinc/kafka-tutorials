while ! echo exit | nc localhost 2181; do sleep 1; done
while ! echo exit | nc localhost 29092; do sleep 1; done
while ! echo exit | nc localhost 8081; do sleep 1; done
while ! echo exit | nc localhost 8088; do sleep 1; done
