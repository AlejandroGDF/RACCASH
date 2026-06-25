package com.raccooncash.api.deuda;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeudaRepositorio extends JpaRepository<Deuda, Long> {
    List<Deuda> findAllByActiveTrueOrderByCreatedAtDesc();
    List<Deuda> findAllByActiveTrue();
    Optional<Deuda> findByIdAndActiveTrue(Long id);
}