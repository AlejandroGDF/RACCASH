package com.raccooncash.api.transaccion;

import com.raccooncash.api.savinggoal.SavingGoal;
import com.raccooncash.api.presupuesto.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepositorio extends JpaRepository<Transaccion, Long>, TransaccionRepositorioPersonalizado {
    @Query("""
            SELECT t FROM TransaccionFinanciera t
            JOIN FETCH t.account account
            LEFT JOIN FETCH t.toAccount destination
            LEFT JOIN FETCH t.category category
            WHERE t.id = :id
              AND (t.active = true OR t.active IS NULL)
            """)
    Optional<Transaccion> findActiveById(@Param("id") Long id);

    List<Transaccion> findBySavingGoalAndActiveTrue(SavingGoal savingGoal);

    @Query("""
            SELECT t FROM TransaccionFinanciera t
            JOIN FETCH t.account account
            LEFT JOIN FETCH t.toAccount destination
            LEFT JOIN FETCH t.category category
            LEFT JOIN FETCH t.budget budget
            WHERE budget = :budget
              AND (t.active = true OR t.active IS NULL)
            ORDER BY t.date DESC
            """)
    List<Transaccion> findByBudgetAndActive(@Param("budget") Presupuesto budget);
}
