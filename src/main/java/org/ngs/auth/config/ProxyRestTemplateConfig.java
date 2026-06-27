package org.ngs.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class ProxyRestTemplateConfig {

    @Autowired
    private ProxyConfig proxyConfig;

    @Bean("proxyRestTemplate")
    public RestTemplate createProxyRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyConfig.getHostName(), proxyConfig.getPort()));
        factory.setProxy(proxy);
        return new RestTemplate(factory);
    }
}
