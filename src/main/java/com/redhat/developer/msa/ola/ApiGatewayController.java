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
package com.redhat.developer.msa.ola;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import feign.hystrix.HystrixFeign;

@RestController
public class ApiGatewayController {

    // List of services
    private static final List<String> services = Arrays.asList("hello", "ola", "hola", "aloha", "bonjour");

    // Regex to extract the hostname of an URL
    private Pattern urlPattern = Pattern.compile("http://(?<url>.*):8080/.*");

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/api", produces = "application/json")
    public List<String> api() {
        // Use Java 8 Streams (map/collect) to invoke the services
        return services.stream()
            .parallel()
            // Creates URLs endpoints
            .map(s -> String.format("http://%s:8080/api/%s", s, s))
            // Creates a Feign Target with fallback support using the Greeting interface
            .map(url -> HystrixFeign.builder().target(Greeting.class, url, () -> {
                // Fallback implementation
                Matcher m = urlPattern.matcher(url);
                String service = url;
                if (m.find()) {
                    service = m.group("url");
                }
                return service + " response (Fallback)";
            }))
            // Invokes the Feign target through the Greeting interface
            .map(Greeting::sayHi)
            // Return the result as List<String>
            .collect(Collectors.toList());
    }

}
