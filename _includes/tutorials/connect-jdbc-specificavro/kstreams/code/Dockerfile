FROM alpine:3.4
WORKDIR /db
ADD cities.sql /db
RUN apk add --update sqlite
RUN sqlite3 geos.db < /db/cities.sql

ENTRYPOINT ["sqlite3"]
