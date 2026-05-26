package br.com.antonio.banking.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AccountsApplicationTest {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring sobe sem erros
    }
}