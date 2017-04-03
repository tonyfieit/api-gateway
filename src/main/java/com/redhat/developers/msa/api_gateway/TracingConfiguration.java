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

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.developers.msa.api_gateway.feign.GenericFeignClients;

import brave.opentracing.BraveTracer;
import feign.Logger;
import feign.httpclient.ApacheHttpClient;
import feign.hystrix.HystrixFeign;
import feign.opentracing.TracingClient;
import feign.opentracing.hystrix.TracingConcurrencyStrategy;
import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

/**
 * @author Pavol Loffay
 */
@Configuration
public class TracingConfiguration {

    @Bean
    public Tracer tracer() {
        String zipkinServerUrl = System.getenv("ZIPKIN_SERVER_URL");
        if (zipkinServerUrl == null) {
            return NoopTracerFactory.create();
        }

        System.out.println("Using Zipkin tracer");
        Reporter<Span> reporter = AsyncReporter.builder(URLConnectionSender.create(zipkinServerUrl + "/api/v1/spans"))
                .build();
        brave.Tracer braveTracer = brave.Tracer.newBuilder().localServiceName("api-gateway").reporter(reporter).build();
        return BraveTracer.wrap(braveTracer);
    }

    public static <T> T createFeign(Tracer tracer, Class<T> clazz, String serviceName, T fallBack) {
        TracingConcurrencyStrategy.register();
        return HystrixFeign.builder()
                .client(new TracingClient(new ApacheHttpClient(HttpClientBuilder.create().build()), tracer))
                .logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
                .target(clazz, String.format("http://%s:8080/", serviceName), fallBack);
    }

    @Bean
    public GenericFeignClients.HolaFeignClient holaService(Tracer tracer) {
        return new GenericFeignClients.HolaFeignClient(tracer);
    }

    @Bean
    public GenericFeignClients.AlohaFeignClient alohaService(Tracer tracer) {
        return new GenericFeignClients.AlohaFeignClient(tracer);
    }

    @Bean
    public GenericFeignClients.OlaFeignClient olaService(Tracer tracer) {
        return new GenericFeignClients.OlaFeignClient(tracer);
    }

    @Bean
    public GenericFeignClients.BonjourFeignClient bonjourService(Tracer tracer) {
        return new GenericFeignClients.BonjourFeignClient(tracer);
    }
}
