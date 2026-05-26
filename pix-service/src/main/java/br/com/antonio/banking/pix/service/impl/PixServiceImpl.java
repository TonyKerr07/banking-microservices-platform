package br.com.antonio.banking.pix.service.impl;

import br.com.antonio.banking.common.dto.PageResponse;
import br.com.antonio.banking.common.exception.ConflictException;
import br.com.antonio.banking.common.exception.ResourceNotFoundException;
import br.com.antonio.banking.common.exception.UnprocessableException;
import br.com.antonio.banking.pix.client.AccountsClient;
import br.com.antonio.banking.pix.domain.entity.PixKey;
import br.com.antonio.banking.pix.domain.entity.PixTransaction;
import br.com.antonio.banking.pix.domain.enums.PixKeyStatus;
import br.com.antonio.banking.pix.domain.enums.PixKeyType;
import br.com.antonio.banking.pix.domain.enums.PixTransactionStatus;
import br.com.antonio.banking.pix.dto.request.CreatePixKeyRequest;
import br.com.antonio.banking.pix.dto.request.SendPixRequest;
import br.com.antonio.banking.pix.dto.response.PixKeyResponse;
import br.com.antonio.banking.pix.dto.response.PixTransactionResponse;
import br.com.antonio.banking.pix.exception.PixKeyNotFoundException;
import br.com.antonio.banking.pix.mapper.PixMapper;
import br.com.antonio.banking.pix.repository.PixKeyRepository;
import br.com.antonio.banking.pix.repository.PixTransactionRepository;
import br.com.antonio.banking.pix.service.PixService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PixServiceImpl implements PixService {

    /**
     * BACEN rule: maximum 5 active PIX keys per account (natural person).
     * Real banks differentiate between PF (5) and PJ (20) — simplified here.
     */
    private static final int MAX_KEYS_PER_ACCOUNT = 5;

    private final PixKeyRepository pixKeyRepository;
    private final PixTransactionRepository pixTransactionRepository;
    private final PixMapper pixMapper;
    private final AccountsClient accountsClient;

    // ── PIX Keys ───────────────────────────────────────────────

    @Override
    @Transactional
    public PixKeyResponse registerKey(CreatePixKeyRequest request) {
        long activeKeys = pixKeyRepository.countByAccountIdAndStatus(
                request.accountId(), PixKeyStatus.ACTIVE
        );

        if (activeKeys >= MAX_KEYS_PER_ACCOUNT) {
            throw new UnprocessableException(
                    "Account has reached the maximum of %d active PIX keys.".formatted(MAX_KEYS_PER_ACCOUNT)
            );
        }

        String resolvedKeyValue = resolveKeyValue(request.keyType(), request.keyValue());

        if (pixKeyRepository.existsByKeyValueAndStatus(resolvedKeyValue, PixKeyStatus.ACTIVE)) {
            throw new ConflictException(
                    "PIX key already registered: " + maskKeyValue(resolvedKeyValue)
            );
        }

        PixKey pixKey = PixKey.builder()
                .accountId(request.accountId())
                .keyType(request.keyType())
                .keyValue(resolvedKeyValue)
                .status(PixKeyStatus.ACTIVE)
                .build();

        PixKey saved = pixKeyRepository.save(pixKey);
        log.info("PIX key registered: id={}, type={}, account={}",
                saved.getId(), saved.getKeyType(), saved.getAccountId());
        return pixMapper.toKeyResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PixKeyResponse> listKeysByAccount(UUID accountId) {
        return pixKeyRepository.findByAccountIdAndStatus(accountId, PixKeyStatus.ACTIVE)
                .stream()
                .map(pixMapper::toKeyResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PixKeyResponse lookupKey(String keyValue) {
        PixKey key = pixKeyRepository.findByKeyValueAndStatus(keyValue, PixKeyStatus.ACTIVE)
                .orElseThrow(() -> new PixKeyNotFoundException(keyValue));
        return pixMapper.toKeyResponse(key);
    }

    @Override
    @Transactional
    public void deleteKey(UUID id) {
        PixKey key = pixKeyRepository.findById(id)
                .orElseThrow(() -> new PixKeyNotFoundException(id));

        if (!key.isActive()) {
            throw new UnprocessableException("PIX key is already deleted.");
        }

        key.delete();
        pixKeyRepository.save(key);
        log.info("PIX key {} deleted (soft delete)", id);
    }

    // ── PIX Transactions ───────────────────────────────────────

    @Override
    @Transactional
    public PixTransactionResponse sendPix(SendPixRequest request) {
        // ── Validate source account ──────────────────────────────
        br.com.antonio.banking.pix.client.dto.AccountInfo sourceAccount =
                accountsClient.findById(request.sourceAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Account", "id", request.sourceAccountId()));

        if (!sourceAccount.isActive()) {
            throw new UnprocessableException(
                    "Source account is not active. Current status: " + sourceAccount.status());
        }

        // ── Validate target PIX key ──────────────────────────────
        if (!pixKeyRepository.existsByKeyValueAndStatus(
                request.targetPixKey(), PixKeyStatus.ACTIVE)) {
            throw new ResourceNotFoundException("PixKey", "keyValue", request.targetPixKey());
        }

        PixKey targetKey = pixKeyRepository
                .findByKeyValueAndStatus(request.targetPixKey(), PixKeyStatus.ACTIVE)
                .orElseThrow(() -> new PixKeyNotFoundException(request.targetPixKey()));

        if (targetKey.getAccountId().equals(request.sourceAccountId())) {
            throw new UnprocessableException("Cannot send PIX to yourself.");
        }

        String endToEndId = "E" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        PixTransaction tx = PixTransaction.builder()
                .sourceAccountId(request.sourceAccountId())
                .targetPixKey(request.targetPixKey())
                .targetAccountId(targetKey.getAccountId())
                .amount(request.amount())
                .description(request.description())
                .status(PixTransactionStatus.COMPLETED)
                .endToEndId(endToEndId)
                .build();

        PixTransaction saved = pixTransactionRepository.save(tx);
        log.info("PIX sent: endToEndId={}, amount={}, from={} to={}",
                endToEndId, request.amount(), request.sourceAccountId(), targetKey.getAccountId());
        return pixMapper.toTransactionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PixTransactionResponse findTransactionById(UUID id) {
        return pixMapper.toTransactionResponse(
                pixTransactionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("PixTransaction", "id", id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PixTransactionResponse> listTransactionsByAccount(
            UUID accountId, Pageable pageable) {
        return PageResponse.from(
                pixTransactionRepository.findAllByAccountId(accountId, pageable),
                pixMapper::toTransactionResponse
        );
    }

    // ── Private helpers ────────────────────────────────────────

    private String resolveKeyValue(PixKeyType type, String keyValue) {
        if (type == PixKeyType.RANDOM) {
            return UUID.randomUUID().toString();
        }
        if (keyValue == null || keyValue.isBlank()) {
            throw new UnprocessableException(
                    "Key value is required for type: " + type
            );
        }
        return keyValue.trim();
    }

    private String maskKeyValue(String value) {
        if (value == null || value.length() <= 4) return "****";
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}