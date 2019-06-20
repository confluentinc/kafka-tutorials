while [ $(curl -s -o /dev/null -w %{http_code} http://localhost:8088/) -eq 000 ] ; do sleep 2 ; done;
