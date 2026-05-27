package br.com.antonio.banking.accounts;

import br.com.antonio.banking.accounts.domain.enums.AccountType;
import br.com.antonio.banking.accounts.dto.request.CreateAccountRequest;
import br.com.antonio.banking.accounts.dto.response.AccountResponse;
import br.com.antonio.banking.accounts.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests using Testcontainers + real PostgreSQL.
 *
 * Why this matters:
 * - H2 unit tests validate logic; these validate SQL, Flyway migrations,
 *   JPA mappings and HTTP layer all together with the real database engine.
 * - @Testcontainers spins up an isolated PostgreSQL container per test class.
 * - Container is reused across tests in this class (performance).
 *
 * Prerequisites: Docker must be running.
 * Skip on CI without Docker: mvn test -Dgroups="!integration"
 */
@Tag("integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@DisplayName("Accounts Service — Integration Tests (PostgreSQL)")
class AccountsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("accounts_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;

    @BeforeEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/accounts → 201 and persists to PostgreSQL")
    void shouldCreateAccountAndPersist() throws Exception {
        var request = new CreateAccountRequest(
                "João da Silva", "12345678901", AccountType.CHECKING, BigDecimal.valueOf(500)
        );

        String body = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.holderName").value("João da Silva"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        AccountResponse response = objectMapper.readValue(body, AccountResponse.class);
        assertThat(accountRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("POST /api/v1/accounts → 409 when document already registered")
    void shouldReturn409WhenDuplicateDocument() throws Exception {
        var request = new CreateAccountRequest(
                "João da Silva", "12345678901", AccountType.CHECKING, BigDecimal.ZERO
        );

        // First — succeeds
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second — conflict
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} → 404 when not found")
    void shouldReturn404WhenAccountNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts → returns paginated list")
    void shouldReturnPaginatedAccounts() throws Exception {
        // Create 3 accounts
        for (int i = 1; i <= 3; i++) {
            var req = new CreateAccountRequest(
                    "Holder " + i, String.format("%011d", i),
                    AccountType.CHECKING, BigDecimal.ZERO
            );
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/v1/accounts?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content").isArray());
    }
}