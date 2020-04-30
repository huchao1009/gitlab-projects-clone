package com.opensource.gitlab.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 初始化RestTemplate配置类
 * Created by sunquan on 2019/10/24.
 */
@EnableConfigurationProperties(RestProperties.class)
@Configuration
public class RestTemplateAutoConfiguration {
    @Autowired
    private RestProperties restProperties;

    @ConditionalOnMissingBean(RestTemplate.class)
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(httpComponentsClientHttpRequestFactory));
        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(restProperties.getConnectionRequestTimeout());
        httpRequestFactory.setConnectTimeout(restProperties.getConnectTimeout());
        httpRequestFactory.setReadTimeout(restProperties.getReadTimeout());
        return httpRequestFactory;
    }
}
