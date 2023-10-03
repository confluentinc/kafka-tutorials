while [ $(curl -s -o /dev/null -w %{http_code} http://localhost:9081/) -eq 000 ] ; do sleep 5 ; done;
# Back off for Flink SQL client container to start.
sleep 5
