produce '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"102","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
sleep 10
produce '{"id":"101","datetime":"'$(date -v-10S +%FT%T.%z)'","pressure":30}' # late of 10 sec
produce '{"id":"101","datetime":"'$(date -v-12S +%FT%T.%z)'","pressure":30}' # late of 12 sec
produce '{"id":"101","datetime":"'$(date -v-13S +%FT%T.%z)'","pressure":30}' # late of 13 sec
produce '{"id":"102","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
sleep 15
produce '{"id":"102","datetime":"'$(date -v-35S +%FT%T.%z)'","pressure":30}' # out of the grace period
export TZ=Asia/Tokyo
produce '{"id":"301","datetime":"'$(date +%FT%T.%z)'","pressure":30}'
produce '{"id":"301","datetime":"'$(date +%FT%T.%z)'","pressure":30}'