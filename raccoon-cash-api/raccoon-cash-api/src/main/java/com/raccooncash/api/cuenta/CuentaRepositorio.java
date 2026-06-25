package com.raccooncash.api.cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CuentaRepositorio extends JpaRepository<Cuenta, Long> {
    List<Cuenta> findAllByActiveTrue();
    Optional<Cuenta> findByIdAndActiveTrue(Long id);
    long countByActiveTrue();
}
