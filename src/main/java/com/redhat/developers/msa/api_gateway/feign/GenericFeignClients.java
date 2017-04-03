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

package com.redhat.developers.msa.api_gateway.feign;

import io.opentracing.Tracer;

public class GenericFeignClients {

    private GenericFeignClients() {}

    public static class AlohaFeignClient extends GenericFeignClient<AlohaService> {

        public AlohaFeignClient(Tracer tracer) {
            super(tracer, AlohaService.class, "aloha", () -> "Aloha response (fallback)");
        }

        @Override
        public String invokeService() {
            return client.aloha();
        }
    }

    public static class BonjourFeignClient extends GenericFeignClient<BonjourService> {

        public BonjourFeignClient(Tracer tracer) {
            super(tracer, BonjourService.class, "bonjour", () -> "Bonjour response (fallback)");
        }

        @Override
        public String invokeService() {
            return client.bonjour();
        }
    }

    public static class HolaFeignClient extends GenericFeignClient<HolaService> {

        public HolaFeignClient(Tracer tracer) {
            super(tracer, HolaService.class, "hola", () -> "Hola response (fallback)");
        }

        @Override
        public String invokeService() {
            return client.hola();
        }
    }

    public static class OlaFeignClient extends GenericFeignClient<OlaService> {

        public OlaFeignClient(Tracer tracer) {
            super(tracer, OlaService.class, "ola", () -> "Ola response (fallback)");
        }

        @Override
        public String invokeService() {
            return client.ola();
        }
    }
}
