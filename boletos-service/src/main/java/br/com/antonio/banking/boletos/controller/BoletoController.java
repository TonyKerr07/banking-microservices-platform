package br.com.antonio.banking.boletos.controller;

import br.com.antonio.banking.boletos.dto.request.CreateBoletoRequest;
import br.com.antonio.banking.boletos.dto.response.BoletoResponse;
import br.com.antonio.banking.boletos.service.BoletoService;
import br.com.antonio.banking.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boletos")
@RequiredArgsConstructor
public class BoletoController {

    private final BoletoService boletoService;

    @PostMapping
    public ResponseEntity<BoletoResponse> create(
            @Valid @RequestBody CreateBoletoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boletoService.create(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<BoletoResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(boletoService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoletoResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(boletoService.findById(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<BoletoResponse> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(boletoService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BoletoResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(boletoService.cancel(id));
    }
}