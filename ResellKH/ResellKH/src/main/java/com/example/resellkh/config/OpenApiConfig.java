package com.example.resellkh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("https://moved-gar-pet.ngrok-free.app"); // 🔥 Force HTTPS

        return new OpenAPI()
                .components(new Components())
                .addServersItem(server)
                .info(new Info().title("ResellKH API").version("1.0"));
    }
}
