package com.basktpay.dfsapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class FileConfiguration {
    @Value("${app.key}")
    private String appKey;

    @Bean
    public OpenAPI myOpenAPI() {
        Info info = new Info()
                .title("File API")
                .version("1.0")
                .description("This API exposes endpoints to view Road Status.");
        return new OpenAPI().info(info);
    }
}
