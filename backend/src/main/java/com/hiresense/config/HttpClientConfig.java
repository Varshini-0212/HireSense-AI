package com.hiresense.config;
import org.springframework.context.annotation.*; import org.springframework.web.client.RestTemplate;
@Configuration public class HttpClientConfig { @Bean RestTemplate restTemplate(){return new RestTemplate();} }
