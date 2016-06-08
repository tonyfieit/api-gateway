package com.redhat.developers.msa.api_gateway.feign;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerSpan;
import com.redhat.developers.msa.api_gateway.GenericFeignClient;

@Component
@Scope(value = "singleton")
public class FeignClientFactory {

    @Autowired
    private Brave brave;

    private List<GenericFeignClient<?>> services;

    /**
     * Creates a list of Feign Clients for different services
     * 
     * @return
     */
    public List<GenericFeignClient<?>> getFeignClients() {
        if (services == null) {
            services = new ArrayList<>();
            services.add(new AlohaFeignClient());
            services.add(new BonjourFeignClient());
            services.add(new HolaFeignClient());
            services.add(new OlaFeignClient());
        }
        return services;
    }

    class AlohaFeignClient extends GenericFeignClient<AlohaService> {

        public AlohaFeignClient() {
            super(AlohaService.class, "aloha", () -> "Aloha response (fallback)", brave);
        }

        @Override
        public String invokeService(ServerSpan serverSpan) {
            return createFeign(serverSpan).aloha();
        }

    }

    class BonjourFeignClient extends GenericFeignClient<BonjourService> {

        public BonjourFeignClient() {
            super(BonjourService.class, "bonjour", () -> "Bonjour response (fallback)", brave);
        }

        @Override
        public String invokeService(ServerSpan serverSpan) {
            return createFeign(serverSpan).bonjour();
        }

    }

    class HolaFeignClient extends GenericFeignClient<HolaService> {

        public HolaFeignClient() {
            super(HolaService.class, "hola", () -> "Hola response (fallback)", brave);
        }

        @Override
        public String invokeService(ServerSpan serverSpan) {
            return createFeign(serverSpan).hola();
        }

    }

    class OlaFeignClient extends GenericFeignClient<OlaService> {

        public OlaFeignClient() {
            super(OlaService.class, "ola", () -> "Ola response (fallback)", brave);
        }

        @Override
        public String invokeService(ServerSpan serverSpan) {
            return createFeign(serverSpan).ola();
        }

    }

}
