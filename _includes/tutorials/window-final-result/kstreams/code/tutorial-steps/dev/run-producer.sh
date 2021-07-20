produce '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"102","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
sleep 10
produce '{"id":"101","datetime":"'$(date -v-10S +%FT%T.%z)'","pressure":30}' # late of 10 sec
produce '{"id":"101","datetime":"'$(date -v-15S +%FT%T.%z)'","pressure":30}' # late of 15 sec
produce '{"id":"101","datetime":"'$(date -v-60S +%FT%T.%z)'","pressure":30}' # late of 01 min
produce '{"id":"102","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
sleep 10
produce '{"id":"102","datetime":"'$(date -v-60S +%FT%T.%z)'","pressure":30}' # out of the grace period
export TZ=Asia/Tokyo
produce '{"id":"301","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"301","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
sleep 10
produce '{"id":"XXX","datetime":"'$(date +%FT%T.%z)'","pressure":30}'