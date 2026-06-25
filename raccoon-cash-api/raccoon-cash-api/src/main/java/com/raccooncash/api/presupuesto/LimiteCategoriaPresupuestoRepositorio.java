package com.raccooncash.api.presupuesto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LimiteCategoriaPresupuestoRepositorio extends JpaRepository<LimiteCategoriaPresupuesto, Long> {
    List<LimiteCategoriaPresupuesto> findAllByBudgetId(Long budgetId);
    Optional<LimiteCategoriaPresupuesto> findByIdAndBudgetId(Long id, Long budgetId);
}