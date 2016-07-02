FROM fabric8/java-jboss-openjdk8-jdk:1.1.4

ENV JAVA_APP_JAR api-gateway.jar
ENV AB_ENABLED jolokia
ENV AB_JOLOKIA_AUTH_OPENSHIFT true


EXPOSE 8080

ADD target/api-gateway.jar /app/