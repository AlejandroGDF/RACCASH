package com.raccooncash.api.deuda;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoDeudaRepositorio extends JpaRepository<PagoDeuda, Long> {
    List<PagoDeuda> findAllByDebtIdAndActiveTrueOrderByPaymentDateDescCreatedAtDesc(Long debtId);
    Optional<PagoDeuda> findByIdAndDebtIdAndActiveTrue(Long id, Long debtId);
    boolean existsByDebtIdAndActiveTrue(Long debtId);
}
