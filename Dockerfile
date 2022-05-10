#FROM adoptopenjdk/openjdk11:alpine-jre
FROM eclipse-temurin:11.0.13_8-jre-focal
ARG CA_CERTS_PATH=./ca
ARG JAR_FILE=build/libs/*.jar
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER root
COPY ${CA_CERTS_PATH}/rootCAPublic.pem /usr/share/ca-certificates/rootCAPublic.pem
RUN echo "rootCAPublic.pem" >>/etc/ca-certificates.conf
COPY ${CA_CERTS_PATH}/rootCAPublic.pem $JAVA_HOME/lib/security/rootCAPublic.pem
RUN \
    cd $JAVA_HOME/lib/security \
    && keytool -import -alias rootCAPublic -noprompt -storepass changeit -cacerts -trustcacerts -file rootCAPublic.pem
USER spring:spring
COPY --chown=spring:spring ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]