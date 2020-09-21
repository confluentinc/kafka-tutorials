# Since we're using ksqlDB with embedded connect, we wait for it to start
# up so that the datagen connector is running before we continue. 
docker exec -it ksqldb bash -c 'echo -e "\n\n  Waiting for ksqlDB to be available before launching CLI\n"; while : ; do curl_status=$(curl -s -o /dev/null -w %{http_code} http://ksqldb:8088/info) ; echo -e $(date) " ksqlDB server listener HTTP state: " $curl_status " (waiting for 200)" ; if [ $curl_status -eq 200 ] ; then  break ; fi ; sleep 5 ; done'
