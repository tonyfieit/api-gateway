/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.developers.msa.api_gateway;

import java.util.LinkedList;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hystrix.metrics.servlet.HystrixEventStreamServlet;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.HystrixConfigurationDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.util.toolbox.AggregationStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableConfigurationProperties(ServiceConfiguration.class)
public class ApiGatewayApplication {

	@Value("${service.host}")
	private String serviceHost;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}


	/**
	 * Bind the Camel servlet at the "/api" context path.
	 */
	@Bean
	ServletRegistrationBean camelServletRegistrationBean() {
		ServletRegistrationBean mapping = new ServletRegistrationBean();
		mapping.setServlet(new CamelHttpTransportServlet());
		mapping.addUrlMappings("/api/*");
		mapping.setName("CamelServlet");
		mapping.setLoadOnStartup(1);

		return mapping;
	}

	/**
	 * Bind the Hystrix servlet to /hystrix.stream
	 */
	@Bean
	ServletRegistrationBean hystrixServletRegistrationBean() {
		ServletRegistrationBean mapping = new ServletRegistrationBean();
		mapping.setServlet(new HystrixEventStreamServlet());
		mapping.addUrlMappings("/hystrix.stream");
		mapping.setName("HystrixEventStreamServlet");

		return mapping;
	}


	@Component
	public class Routes extends RouteBuilder {
		@Override
		public void configure() throws Exception {

			restConfiguration()
					.host(serviceHost)
					.bindingMode(RestBindingMode.json)
					.contextPath("/api/")
					.apiContextPath("/doc")
					.apiProperty("api.title", "API-Gateway  REST API")
					.apiProperty("api.description", "Operations that can be invoked in the api-gateway")
					.apiProperty("api.license.name", "Apache License Version 2.0")
					.apiProperty("api.license.url", "http://www.apache.org/licenses/LICENSE-2.0.html")
					.apiProperty("api.version", "1.0.0");


            HystrixConfigurationDefinition hystrixConfig = new HystrixConfigurationDefinition()
                    .circuitBreakerRequestVolumeThreshold(5)
                    .executionTimeoutInMilliseconds(1000);

			from("direct:aloha")
					.removeHeaders("*")
					.setHeader(Exchange.HTTP_METHOD, constant("GET"))
					.hystrix()
                        .hystrixConfiguration(hystrixConfig)
						.id("aloha")
                        .groupKey("http://aloha:8080/")
						.to("http4://aloha:8080/api/aloha?bridgeEndpoint=true&connectionClose=true")
						.convertBodyTo(String.class)
					.onFallback()
						.transform().constant("Aloha response (fallback)")
					.end();

			from("direct:hola")
					.removeHeaders("*")
					.setHeader(Exchange.HTTP_METHOD, constant("GET"))
					.hystrix()
                        .hystrixConfiguration(hystrixConfig)
                        .id("hola")
                        .groupKey("http://hola:8080/")
						.to("http4://hola:8080/api/hola?bridgeEndpoint=true&connectionClose=true")
						.convertBodyTo(String.class)
					.onFallback()
						.transform().constant("Hola response (fallback)")
					.end();

			from("direct:ola")
					.removeHeaders("*")
					.setHeader(Exchange.HTTP_METHOD, constant("GET"))
					.hystrix()
                        .hystrixConfiguration(hystrixConfig)
                        .id("ola")
                        .groupKey("http://ola:8080/")
						.to("http4://ola:8080/api/ola?bridgeEndpoint=true&connectionClose=true")
						.convertBodyTo(String.class)
					.onFallback()
						.transform().constant("Ola response (fallback)")
					.end();


			from("direct:bonjour")
					.removeHeaders("*")
					.setHeader(Exchange.HTTP_METHOD, constant("GET"))
					.hystrix()
                        .hystrixConfiguration(hystrixConfig)
                        .id("bonjour")
                        .groupKey("http://bonjour:8080/")
						.to("http4://bonjour:8080/api/bonjour?bridgeEndpoint=true&connectionClose=true")
						.convertBodyTo(String.class)
					.onFallback()
						.transform().constant("Bonjour response (fallback)")
					.end();


			rest().get("/ciao")
					.description("Invoke all microservices in parallel")
					.outTypeList(String.class)
					.apiDocs(true)
					.responseMessage().code(200).message("OK").endResponseMessage()
					.route()
					.multicast(AggregationStrategies.flexible().accumulateInCollection(LinkedList.class))
					.parallelProcessing()
						.to("direct:aloha")
						.to("direct:hola")
						.to("direct:ola")
						.to("direct:bonjour")
					.end()
					.setHeader("Access-Control-Allow-Credentials", constant("true"))
					.setHeader("Access-Control-Allow-Origin", header("Origin"));


		}
	}

}
