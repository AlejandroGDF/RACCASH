package com.raccooncash.api.categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class CategoriaSolicitud {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    @NotNull(message = "El tipo de categoria es obligatorio")
    private TipoCategoria type;
    private String color;
    private String icon;
    private Long parentCategoryId;
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
}
