package com.raccooncash.api.presupuesto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepositorio extends JpaRepository<Presupuesto, Long> {
    List<Presupuesto> findAllByActiveTrueOrderByStartDateDesc();
    Optional<Presupuesto> findByIdAndActiveTrue(Long id);
    long countByActiveTrue();
}