package com.example.resilient_api.application.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bootcamp Metrics & Reporting API")
                        .version("1.0.0")
                        .description("""
                                API para la gestión de métricas y reportes de bootcamps.
                                
                                **Funcionalidades principales:**
                                - Registrar reportes de bootcamps (automático, interno)
                                - Consultar el bootcamp más popular (solo ADMIN)
                                
                                **Autenticación:**
                                El endpoint de consulta requiere autenticación JWT con rol ADMIN.
                                Usa el endpoint de login del microservicio users-api para obtener un token.
                                """)
                        .contact(new Contact()
                                .name("Bootcamp Development Team")
                                .email("support@bootcamp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Introduce el token JWT obtenido del endpoint de login")));
    }
}
