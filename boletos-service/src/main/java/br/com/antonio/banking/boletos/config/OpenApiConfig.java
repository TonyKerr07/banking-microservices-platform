package br.com.antonio.banking.boletos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Boletos Service API")
                        .description("Boleto issuance, payment and cancellation")
                        .version("v1.0.0")
                        .contact(new Contact().name("Antonio").url("https://github.com/TonyKerr07")))
                .servers(List.of(new Server().url("http://localhost:8083").description("Local")));
    }
}