package com.raccooncash.api.categoria;
import java.time.LocalDateTime;
public class CategoriaRespuesta {
    private Long id;
    private String name;
    private TipoCategoria type;
    private String color;
    private String icon;
    private Long parentCategoryId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public CategoriaRespuesta() {
    }
    public CategoriaRespuesta(Categoria category) {
        this.id = category.getId();
        this.name = category.getName();
        this.type = category.getType();
        this.color = category.getColor();
        this.icon = category.getIcon();
        this.parentCategoryId = category.getParentCategory() != null ? category.getParentCategory().getId() : null;
        this.active = category.getActive();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public TipoCategoria getType() {
        return type;
    }
    public void setType(TipoCategoria type) {
        this.type = type;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public Long getParentCategoryId() {
        return parentCategoryId;
    }
    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
