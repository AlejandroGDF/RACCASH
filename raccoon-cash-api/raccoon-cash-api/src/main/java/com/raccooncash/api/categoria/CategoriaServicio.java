package com.raccooncash.api.categoria;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class CategoriaServicio {
    private final CategoriaRepositorio categoryRepository;
    public CategoriaServicio(CategoriaRepositorio categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Transactional(readOnly = true)
    public List<CategoriaRespuesta> getCategories(TipoCategoria type) {
        List<Categoria> categories = type == null
                ? categoryRepository.findAllByActiveTrue()
                : categoryRepository.findAllByTypeAndActiveTrue(type);
        return categories.stream()
                .map(CategoriaRespuesta::new)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public CategoriaRespuesta getCategoryById(Long id) {
        Categoria category = findActiveCategory(id);
        return new CategoriaRespuesta(category);
    }
    @Transactional
    public CategoriaRespuesta createCategory(CategoriaSolicitud request) {
        Categoria category = new Categoria();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());
        category.setParentCategory(resolveParentCategory(request.getParentCategoryId(), null, request.getType()));
        category.setActive(true);
        Categoria savedCategory = categoryRepository.save(category);
        return new CategoriaRespuesta(savedCategory);
    }
    @Transactional
    public CategoriaRespuesta updateCategory(Long id, CategoriaSolicitud request) {
        Categoria category = findActiveCategory(id);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());
        category.setParentCategory(resolveParentCategory(request.getParentCategoryId(), category.getId(), request.getType()));
        Categoria updatedCategory = categoryRepository.save(category);
        return new CategoriaRespuesta(updatedCategory);
    }
    @Transactional
    public void deleteCategory(Long id) {
        Categoria category = findActiveCategory(id);
        category.setActive(false);
        categoryRepository.save(category);
    }
    private Categoria resolveParentCategory(Long parentCategoryId, Long categoryId, TipoCategoria type) {
        if (parentCategoryId == null || parentCategoryId == 0) {
            return null;
        }

        if (parentCategoryId.equals(categoryId)) {
            throw new SolicitudIncorrectaException("La categoria no puede ser su propia categoria padre");
        }

        Categoria parentCategory = findActiveCategory(parentCategoryId);
        if (parentCategory.getType() != type) {
            throw new SolicitudIncorrectaException("La categoria padre debe ser del mismo tipo");
        }

        return parentCategory;
    }
    private Categoria findActiveCategory(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));
    }
}
