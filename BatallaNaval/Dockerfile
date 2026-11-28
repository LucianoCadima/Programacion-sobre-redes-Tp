# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /src
COPY src/main/java ./fuentes
RUN mkdir /out && javac -encoding UTF-8 -d /out $(find fuentes -name '*.java')

# ---------- Etapa 2: ejecutar (imagen liviana, solo JRE) ----------
FROM eclipse-temurin:17-jre-alpine
ENV LANG=C.UTF-8 \
    PUERTO=5555 \
    HISTORIAL=/app/data/historial.txt
WORKDIR /app
RUN adduser -D -u 1000 juego && mkdir -p /app/data && chown juego /app/data
COPY --from=build /out /app/classes
USER juego
EXPOSE 5555
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-cp", "/app/classes", "batallanaval.servidor.MainServidor"]
