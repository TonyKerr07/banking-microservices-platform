package br.com.antonio.banking.boletos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoletosApplication {
    public static void main(String[] args) {
        SpringApplication.run(BoletosApplication.class, args);
    }
}