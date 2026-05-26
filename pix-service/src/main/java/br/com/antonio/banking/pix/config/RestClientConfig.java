package br.com.antonio.banking.pix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient accountsRestClient(
            @Value("${banking.accounts-service.url}") String url,
            RestClient.Builder builder
    ) {
        return builder
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}