echo '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}' >> out.txt
echo '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}' >> out.txt
echo '{"id":"101","datetime":"'$(date +%FT%T.%z)'","pressure":30}' >> out.txt
echo '{"id":"102","datetime":"'$(date +%FT%T.%z)'","pressure":30}' >> out.txt
sleep 10
echo '{"id":"101","datetime":"'$(date -v-10S +%FT%T.%z)'","pressure":30}' >> out.txt # late of 10 sec
echo '{"id":"101","datetime":"'$(date -v-12S +%FT%T.%z)'","pressure":30}' >> out.txt # late of 12 sec
echo '{"id":"101","datetime":"'$(date -v-13S +%FT%T.%z)'","pressure":30}' >> out.txt # late of 13 sec
echo '{"id":"102","datetime":"'$(date +%FT%T.%z)'","pressure":30}' >> out.txt
sleep 15
echo '{"id":"102","datetime":"'$(date -v-35S +%FT%T.%z)'","pressure":30}' >> out.txt # out of the grace period
export TZ=Asia/Tokyo
echo '{"id":"301","datetime":"'$(date +%FT%T.%z)'","pressure":30}' >> out.txt
echo '{"id":"301","datetime":"'$(date +%FT%T.%z)'","pressure":30}' >> out.txt