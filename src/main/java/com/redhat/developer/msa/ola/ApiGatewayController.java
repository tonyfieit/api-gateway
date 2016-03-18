/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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

import com.netflix.hystrix.HystrixCommandProperties;
import feign.RequestLine;
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

	static {
		HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(2000);
	}

	private static final List<String> services = Arrays.asList("hello", "ola", "hola", "aloha", "bonjour");

	@CrossOrigin
	@RequestMapping(method = RequestMethod.GET, value = "/api", produces = "application/json")
	public List<String> ola() {
		return services.stream()
				.parallel()
				.map(s -> String.format("http://%s/api", s))
				.map(url -> HystrixFeign.builder().target(Greeting.class, url, () -> "Bye"))
				.map(Greeting::sayHi)
				.collect(Collectors.toList());
	}

	interface Greeting {
		@RequestLine("GET /")
		String sayHi();
	}

}
