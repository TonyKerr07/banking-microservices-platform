package br.com.antonio.banking.accounts.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String port;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Accounts Service API")
                        .description("Bank account management — creation, status and balance")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Antonio")
                                .url("https://github.com/TonyKerr07")))
                .servers(List.of(
                        new Server().url("http://localhost:" + port).description("Local")
                ));
    }
}