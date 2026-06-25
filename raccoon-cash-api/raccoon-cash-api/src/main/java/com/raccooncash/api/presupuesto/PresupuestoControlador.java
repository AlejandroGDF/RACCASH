package com.raccooncash.api.presupuesto;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/presupuestos", "/api/budgets"})
public class PresupuestoControlador {

    private final PresupuestoServicio budgetService;
    private final LimiteCategoriaPresupuestoServicio limitService;

    public PresupuestoControlador(PresupuestoServicio budgetService, LimiteCategoriaPresupuestoServicio limitService) {
        this.budgetService = budgetService;
        this.limitService = limitService;
    }

    @GetMapping
    public ResponseEntity<List<PresupuestoRespuesta>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoRespuesta> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @PostMapping
    public ResponseEntity<PresupuestoRespuesta> createBudget(@Valid @RequestBody PresupuestoSolicitud request) {
        return ResponseEntity.ok(budgetService.createBudget(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoRespuesta> updateBudget(@PathVariable Long id, @Valid @RequestBody PresupuestoSolicitud request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ResumenPresupuestoRespuesta> getBudgetSummary(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetSummary(id));
    }

    @GetMapping("/{budgetId}/categories")
    public ResponseEntity<List<LimiteCategoriaPresupuestoRespuesta>> getCategoryLimits(@PathVariable Long budgetId) {
        return ResponseEntity.ok(limitService.getLimits(budgetId));
    }

    @PostMapping("/{budgetId}/categories")
    public ResponseEntity<LimiteCategoriaPresupuestoRespuesta> createCategoryLimit(@PathVariable Long budgetId,
                                                                            @Valid @RequestBody LimiteCategoriaPresupuestoSolicitud request) {
        return ResponseEntity.ok(limitService.createLimit(budgetId, request));
    }

    @PutMapping("/{budgetId}/categories/{limitId}")
    public ResponseEntity<LimiteCategoriaPresupuestoRespuesta> updateCategoryLimit(@PathVariable Long budgetId,
                                                                            @PathVariable Long limitId,
                                                                            @Valid @RequestBody LimiteCategoriaPresupuestoSolicitud request) {
        return ResponseEntity.ok(limitService.updateLimit(budgetId, limitId, request));
    }

    @DeleteMapping("/{budgetId}/categories/{limitId}")
    public ResponseEntity<Void> deleteCategoryLimit(@PathVariable Long budgetId, @PathVariable Long limitId) {
        limitService.deleteLimit(budgetId, limitId);
        return ResponseEntity.noContent().build();
    }
}
