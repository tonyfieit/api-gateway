/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.redhat.developers.msa.api_gateway;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.HystrixConfigurationDefinition;
import org.springframework.stereotype.Component;

@Component
public class CamelServiceRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /*
         * Common hystrix configuration
         */
        HystrixConfigurationDefinition hystrixConfig = new HystrixConfigurationDefinition()
                .circuitBreakerRequestVolumeThreshold(5)
                .executionTimeoutInMilliseconds(1000);

        /*
         * Definition of the external services: aloha
         */

        from("direct:aloha")
                .id("aloha")
                .removeHeaders("*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .hystrix()
                    .hystrixConfiguration(hystrixConfig)
                    .id("aloha")
                    .groupKey("http://aloha:8080/")
                    .to("http4:aloha:8080/api/aloha?bridgeEndpoint=true&connectionClose=true&http&httpClientConfigurer=#zipkinConfigurer")
                    .convertBodyTo(String.class)
                .onFallback()
                    .transform().constant("Aloha response (fallback)")
                .end();

        /*
         * Definition of the external services: hola
         */

        from("direct:hola")
                .id("hola")
                .removeHeaders("*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .hystrix()
                    .hystrixConfiguration(hystrixConfig)
                    .id("hola")
                    .groupKey("http://hola:8080/")
                    .to("http4:hola:8080/api/hola?bridgeEndpoint=true&connectionClose=true&httpClientConfigurer=#zipkinConfigurer")
                    .convertBodyTo(String.class)
                .onFallback()
                    .transform().constant("Hola response (fallback)")
                .end();

        /*
         * Definition of the external services: ola
         */

        from("direct:ola")
                .id("ola")
                .removeHeaders("*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .hystrix()
                    .hystrixConfiguration(hystrixConfig)
                    .id("ola")
                    .groupKey("http://ola:8080/")
                    .to("http4:ola:8080/api/ola?bridgeEndpoint=true&connectionClose=true&httpClientConfigurer=#zipkinConfigurer")
                    .convertBodyTo(String.class)
                .onFallback()
                    .transform().constant("Ola response (fallback)")
                .end();

        /*
         * Definition of the external services: bonjour
         */

        from("direct:bonjour")
                .id("bonjour")
                .removeHeaders("*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .hystrix()
                    .hystrixConfiguration(hystrixConfig)
                    .id("bonjour")
                    .groupKey("http://bonjour:8080/")
                    .to("http4:bonjour:8080/api/bonjour?bridgeEndpoint=true&connectionClose=true&httpClientConfigurer=#zipkinConfigurer")
                    .convertBodyTo(String.class)
                .onFallback()
                    .transform().constant("Bonjour response (fallback)")
                .end();

    }
}
