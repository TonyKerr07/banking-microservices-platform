package br.com.antonio.banking.transfers.client;

import br.com.antonio.banking.common.exception.ServiceUnavailableException;
import br.com.antonio.banking.transfers.client.dto.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;
import java.util.UUID;

/**
 * HTTP client for accounts-service communication.
 *
 * Uses Spring 6.1 RestClient (synchronous, fluent API).
 * In a future iteration, this can be replaced with a reactive WebClient
 * or wrapped with a Resilience4j CircuitBreaker.
 */
@Slf4j
@Component
public class AccountsClient {

    private final RestClient restClient;

    public AccountsClient(RestClient accountsRestClient) {
        this.restClient = accountsRestClient;
    }

    /**
     * Fetches account info from accounts-service.
     * Returns empty Optional if account does not exist (404).
     * Throws ServiceUnavailableException on connectivity errors.
     */
    public Optional<AccountInfo> findById(UUID accountId) {
        log.debug("Calling accounts-service: GET /api/v1/accounts/{}", accountId);
        try {
            AccountInfo response = restClient.get()
                    .uri("/api/v1/accounts/{id}", accountId)
                    .retrieve()
                    .body(AccountInfo.class);
            return Optional.ofNullable(response);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Account not found in accounts-service: {}", accountId);
            return Optional.empty();

        } catch (RestClientException e) {
            log.error("accounts-service call failed for account {}: {}", accountId, e.getMessage());
            throw new ServiceUnavailableException("accounts-service");
        }
    }
}