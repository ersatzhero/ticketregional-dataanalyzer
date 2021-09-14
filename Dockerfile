FROM openjdk:18-jdk-slim-buster

RUN /bin/sh -c set -eux && \
    apt-get update && \
    apt-get install -y --no-install-recommends catdoc && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY xls2csv.sh /app
COPY target/import-ticketregional-0.0.1-SNAPSHOT.jar /app
ENTRYPOINT ["java", "-jar import-ticketregional-0.0.1-SNAPSHOT.jar de.ersatzhero.ticketregionalpipe.TicketregionalpipeApplication"]