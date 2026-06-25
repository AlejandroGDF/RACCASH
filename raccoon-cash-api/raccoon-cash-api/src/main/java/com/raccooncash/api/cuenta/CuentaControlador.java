package com.raccooncash.api.cuenta;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class CuentaControlador {

    private final CuentaServicio accountService;

    public CuentaControlador(CuentaServicio accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<CuentaRespuesta>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllActiveAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaRespuesta> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping
    public ResponseEntity<CuentaRespuesta> createAccount(@Valid @RequestBody CuentaSolicitud request) {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaRespuesta> updateAccount(@PathVariable Long id, @Valid @RequestBody CuentaSolicitud request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
