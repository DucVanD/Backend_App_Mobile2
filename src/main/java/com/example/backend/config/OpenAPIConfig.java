package com.example.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {

        // 🔐 JWT Bearer config
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                // ===== API INFO =====
                .info(new Info()
                        .title("Admin DiDong API")
                        .version("1.0.0")
                        .description("Spring Boot + JWT + Role ADMIN / USER")
                        .contact(new Contact()
                                .name("Admin DiDong Team")
                                .email("admin@didong.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )

                // ===== SERVER =====
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))

                // ===== SECURITY =====
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuth))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}
