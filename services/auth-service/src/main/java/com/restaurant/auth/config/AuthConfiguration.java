package com.restaurant.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfiguration {
}
