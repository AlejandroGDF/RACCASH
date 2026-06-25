package com.raccooncash.api.categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByActiveTrue();
    List<Categoria> findAllByTypeAndActiveTrue(TipoCategoria type);
    Optional<Categoria> findByIdAndActiveTrue(Long id);
    Optional<Categoria> findFirstByNameIgnoreCaseAndTypeAndActiveTrue(String name, TipoCategoria type);
    boolean existsByNameIgnoreCaseAndTypeAndActiveTrue(String name, TipoCategoria type);
}
