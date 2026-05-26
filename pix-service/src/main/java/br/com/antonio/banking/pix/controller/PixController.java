package br.com.antonio.banking.pix.controller;

import br.com.antonio.banking.common.dto.PageResponse;
import br.com.antonio.banking.pix.dto.request.CreatePixKeyRequest;
import br.com.antonio.banking.pix.dto.request.SendPixRequest;
import br.com.antonio.banking.pix.dto.response.PixKeyResponse;
import br.com.antonio.banking.pix.dto.response.PixTransactionResponse;
import br.com.antonio.banking.pix.service.PixService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PixController {

    private final PixService pixService;

    // ── PIX Keys ───────────────────────────────────────────────

    @PostMapping("/api/v1/pix/keys")
    public ResponseEntity<PixKeyResponse> registerKey(
            @Valid @RequestBody CreatePixKeyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pixService.registerKey(request));
    }

    @GetMapping("/api/v1/pix/keys")
    public ResponseEntity<List<PixKeyResponse>> listKeys(
            @RequestParam UUID accountId
    ) {
        return ResponseEntity.ok(pixService.listKeysByAccount(accountId));
    }

    @GetMapping("/api/v1/pix/keys/lookup")
    public ResponseEntity<PixKeyResponse> lookupKey(@RequestParam String key) {
        return ResponseEntity.ok(pixService.lookupKey(key));
    }

    @DeleteMapping("/api/v1/pix/keys/{id}")
    public ResponseEntity<Void> deleteKey(@PathVariable UUID id) {
        pixService.deleteKey(id);
        return ResponseEntity.noContent().build();
    }

    // ── PIX Transactions ───────────────────────────────────────

    @PostMapping("/api/v1/pix/transactions")
    public ResponseEntity<PixTransactionResponse> sendPix(
            @Valid @RequestBody SendPixRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pixService.sendPix(request));
    }

    @GetMapping("/api/v1/pix/transactions/{id}")
    public ResponseEntity<PixTransactionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(pixService.findTransactionById(id));
    }

    @GetMapping("/api/v1/pix/transactions")
    public ResponseEntity<PageResponse<PixTransactionResponse>> listByAccount(
            @RequestParam UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                pixService.listTransactionsByAccount(accountId, PageRequest.of(page, size))
        );
    }
}