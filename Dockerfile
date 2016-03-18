FROM rhel7

RUN yum -y install java && yum clean all

EXPOSE 8080

RUN groupadd -r swuser -g 433 && \
	useradd -u 431 -r -g swuser -d /home/swuser -s /sbin/nologin -c "Docker image user" swuser && \
	mkdir -p /home/swuser && \
	chown -R swuser:swuser /home/swuser

USER swuser

WORKDIR /home/swuser

CMD java -jar api-gateway.jar

ADD target/api-gateway.jar /home/swuser/
