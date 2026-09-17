package com.assic.muni.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.assic.muni.infrastructure.client")
public class FeignClientConfig {
}
