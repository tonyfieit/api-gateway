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

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Brave.Builder;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.httpclient.BraveHttpRequestInterceptor;
import com.github.kristofa.brave.httpclient.BraveHttpResponseInterceptor;
import com.github.kristofa.brave.servlet.BraveServletFilter;

import org.apache.camel.component.http4.HttpClientConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ZipkinConfiguration {

    /**
     * Register the ZipKin Filter to intercept {@link ServerRequestInterceptor} and {@link ServerResponseInterceptor}
     */
    @Bean
    public FilterRegistrationBean zipkinFilter(Brave brave) {
        FilterRegistrationBean registration = new FilterRegistrationBean(BraveServletFilter.create(brave));
        // Explicit mapping to avoid trace on readiness probe
        registration.addUrlPatterns("/api/*");
        return registration;
    }

    /**
     * Adds the brave interceptors to outgoing calls.
     */
    @Bean
    public HttpClientConfigurer zipkinConfigurer(Brave brave) {
        return (clientBuilder) -> {
            clientBuilder.addInterceptorFirst(BraveHttpRequestInterceptor.create(brave));
            clientBuilder.addInterceptorFirst(BraveHttpResponseInterceptor.create(brave));
        };
    }

    /**
     * The instance of {@link Brave} - A instrumentation library for Zipkin
     */
    @Bean
    @Scope(value = "singleton")
    public Brave getBrave() {
        String zipkingServer = System.getenv("ZIPKIN_SERVER_URL");
        Builder builder = new Brave.Builder("api-gateway");
        if (null == zipkingServer) {
            // Default configuration
            System.out.println("No ZIPKIN_SERVER_URL defined. Printing zipkin traces to console.");
            return builder.build();
        } else {
            // Brave configured for a Server
            return builder.spanCollector(HttpSpanCollector.create(System.getenv("ZIPKIN_SERVER_URL"),
                    new EmptySpanCollectorMetricsHandler()))
                    .build();
        }
    }

}
