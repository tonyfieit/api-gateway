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

import java.util.regex.Pattern;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.developers.msa.api_gateway.feign.GenericFeignClients;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.senders.Sender;
import com.uber.jaeger.senders.UDPSender;

import brave.opentracing.BraveTracer;
import feign.Logger;
import feign.httpclient.ApacheHttpClient;
import feign.hystrix.HystrixFeign;
import feign.opentracing.TracingClient;
import feign.opentracing.hystrix.TracingConcurrencyStrategy;
import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.autoconfig.WebTracingConfiguration;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

/**
 * @author Pavol Loffay
 */
@Configuration
public class TracingConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TracingConfiguration.class);
    private static final String SERVICE_NAME = "api-gateway";

    @Bean
    public Tracer tracer() {
        String tracingSystem = System.getenv("TRACING_SYSTEM");
        if ("zipkin".equals(tracingSystem)) {
            log.info("Using Zipkin tracer");
            return zipkinTracer(System.getenv("ZIPKIN_SERVER_URL"));
        } else if ("jaeger".equals(tracingSystem)) {
            log.info("Using Jaeger tracer");
            return jaegerTracer(System.getenv("JAEGER_SERVER_URL"));
        }

        log.info("Using Noop tracer");
        return NoopTracerFactory.create();
    }

    private Tracer zipkinTracer(String url) {
        Reporter<Span> reporter = AsyncReporter.builder(URLConnectionSender.create(url + "/api/v1/spans"))
                .build();
        brave.Tracer braveTracer = brave.Tracer.newBuilder().localServiceName(SERVICE_NAME).reporter(reporter).build();
        return BraveTracer.wrap(braveTracer);
    }

    private Tracer jaegerTracer(String url) {
        Sender sender = new UDPSender(url, 0, 0);
        return new com.uber.jaeger.Tracer.Builder(SERVICE_NAME,
                new RemoteReporter(sender, 100, 50,
                        new Metrics(new StatsFactoryImpl(new NullStatsReporter()))),
                new ProbabilisticSampler(1.0))
                .build();
    }

    @Bean
    public WebTracingConfiguration webTracingConfiguration() {
        return WebTracingConfiguration.builder()
                .withSkipPattern(Pattern.compile("/health"))
                .build();
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
