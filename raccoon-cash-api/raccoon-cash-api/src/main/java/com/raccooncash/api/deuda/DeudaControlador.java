package com.raccooncash.api.deuda;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/debts")
public class DeudaControlador {

    private final DeudaServicio debtService;

    public DeudaControlador(DeudaServicio debtService) {
        this.debtService = debtService;
    }

    @GetMapping
    public ResponseEntity<List<DeudaRespuesta>> getAllDebts(
            @RequestParam(required = false) TipoDeuda type,
            @RequestParam(required = false) EstadoDeuda status,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(debtService.getAllDebts(type, status, accountId, dueFrom, dueTo, overdue, search));
    }

    @GetMapping("/reminders")
    public ResponseEntity<List<DeudaRespuesta>> getPendingReminders() {
        return ResponseEntity.ok(debtService.getPendingReminders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeudaRespuesta> getDebtById(@PathVariable Long id) {
        return ResponseEntity.ok(debtService.getDebtById(id));
    }

    @PostMapping
    public ResponseEntity<DeudaRespuesta> createDebt(@Valid @RequestBody DeudaSolicitud request) {
        return ResponseEntity.ok(debtService.createDebt(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeudaRespuesta> updateDebt(@PathVariable Long id, @Valid @RequestBody DeudaSolicitud request) {
        return ResponseEntity.ok(debtService.updateDebt(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<PagoDeudaRespuesta> addPayment(@PathVariable Long id, @Valid @RequestBody PagoDeudaSolicitud request) {
        return ResponseEntity.ok(debtService.addPayment(id, request));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PagoDeudaRespuesta>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(debtService.getPayments(id));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id, @PathVariable Long paymentId) {
        debtService.deletePayment(id, paymentId);
        return ResponseEntity.noContent().build();
    }
}
