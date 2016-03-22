FROM jboss/base-jdk:8

ADD target/api-gateway.jar /

EXPOSE 8080

CMD java -jar /api-gateway.jar
