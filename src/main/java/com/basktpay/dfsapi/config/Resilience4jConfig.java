package com.basktpay.dfsapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class Resilience4jConfig {
    // No need to configure CircuitBreakerRegistry here
}
