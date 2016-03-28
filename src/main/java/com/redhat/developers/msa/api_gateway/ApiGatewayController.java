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

import feign.hystrix.HystrixFeign;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ApiGatewayController {

    /**
     * The list of services we're invoking through the API Gateway
     */
    private static final List<String> services = Arrays.asList("hello", "ola", "hola", "aloha", "bonjour", "namaste");

    /**
     * This /api REST endpoint uses Java 8 parallel stream to create the Feign, invoke it, and collect the result as a List that
     * will be rendered as a JSON Array.
     * 
     * @return
     */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/api", produces = "application/json")
    public List<String> api() {
        return services.stream()
            .parallel()
            .map(this::createFeign)
            .map(Greeting::sayHi)
            .collect(Collectors.toList());
    }

    /**
     * This is were the "magic" happens: it creates a Feign, which is a proxy interface for remote calling a REST endpoint with
     * Hystrix fallback support.
     * 
     * @param name The service to be invoked.
     * @return The feign pointing to the service URL and with Hystrix fallback.
     */
    private Greeting createFeign(String name) {
        String url = String.format("http://%s:8080/api/%s", name, name);
        return HystrixFeign.builder().target(Greeting.class, url, () -> String.format("%s response (fallback)", name));
    }

}
