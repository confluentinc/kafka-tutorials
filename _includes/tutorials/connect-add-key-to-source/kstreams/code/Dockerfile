FROM alpine:3.4
RUN apk add --update sqlite
RUN mkdir /db
WORKDIR /db
ADD cities.sql /db
RUN sqlite3 geos.db < /db/cities.sql

ENTRYPOINT ["sqlite3"]
