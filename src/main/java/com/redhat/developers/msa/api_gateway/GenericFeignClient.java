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

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.ServerSpan;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.StringServiceNameProvider;
import com.github.kristofa.brave.httpclient.BraveHttpRequestInterceptor;
import com.github.kristofa.brave.httpclient.BraveHttpResponseInterceptor;

import feign.Logger;
import feign.Logger.Level;
import feign.httpclient.ApacheHttpClient;
import feign.hystrix.HystrixFeign;

/**
 * This class constructs a Feign Client to be invoked
 *
 */
public abstract class GenericFeignClient<T> {

    private String serviceName;

    private Class<T> classType;

    private T fallBack;

    private Brave brave;

    /**
     * We need the following information to instantiate a FeignClient
     * 
     * @param classType Service that will be invoked
     * @param serviceName the name of the service. It will be used in the hostname and in zipking tracing
     * @param fallback the fallback implementation
     * @param brave {@link Brave} instance used to configure {@link ClientRequestInterceptor} and
     *        {@link ClientResponseInterceptor}
     */
    public GenericFeignClient(Class<T> classType, String serviceName, T fallback, Brave brave) {
        this.classType = classType;
        this.serviceName = serviceName;
        this.fallBack = fallback;
        this.brave = brave;
    }

    /**
     * This should be implemented to call each service interface using the original {@link ServerSpan}
     * 
     * @param serverSpan The original {@link ServerSpan} received from ZipKin
     * @return Return for each endpoint
     */
    public abstract String invokeService(ServerSpan serverSpan);

    /**
     * This is were the "magic" happens: it creates a Feign, which is a proxy interface for remote calling a REST endpoint with
     * Hystrix fallback support.
     * 
     * @param - The original ServerSpan
     * 
     * @return The feign pointing to the service URL and with Hystrix fallback.
     */
    protected T createFeign(ServerSpan serverSpan) {
        final CloseableHttpClient httpclient =
            HttpClients.custom()
                .addInterceptorFirst(new BraveHttpRequestInterceptor(brave.clientRequestInterceptor(), new StringServiceNameProvider(serviceName), new DefaultSpanNameProvider()))
                .addInterceptorFirst(new BraveHttpResponseInterceptor(brave.clientResponseInterceptor()))
                .build();
        String url = String.format("http://%s:8080/", serviceName);
        return HystrixFeign.builder()
            // Use apache HttpClient which contains the ZipKin Interceptors
            .client(new ApacheHttpClient(httpclient))
            // Bind Zipkin Server Span to Feign Thread
            .requestInterceptor((t) -> brave.serverSpanThreadBinder().setCurrentSpan(serverSpan))
            .logger(new Logger.ErrorLogger()).logLevel(Level.BASIC)
            .target(classType, url, fallBack);
    }

}
