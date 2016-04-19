# api-gateway
API Gateway using Feign and Hystrix.

Build and Deploy api-gateway
-----------------------------

1. Open a command prompt and navigate to the root directory of this api-gateway.
2. Type this command to build and execute the api-gateway:

        mvn clean compile spring-boot:run

Access the api-gateway
----------------------

The application will be running at the following URL: <http://localhost:8080/api>
        
Deploy the application in Openshift
-----------------------------------

1. Make sure to be connected to the Docker Daemon
2. Execute

		mvn clean package docker:build fabric8:json fabric8:apply
