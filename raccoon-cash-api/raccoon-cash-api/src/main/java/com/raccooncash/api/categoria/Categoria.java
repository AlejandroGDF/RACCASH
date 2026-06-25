package com.raccooncash.api.categoria;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String icon;

    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCategoria type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Categoria parentCategory;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Categoria() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public TipoCategoria getType() {
        return type;
    }

    public void setType(TipoCategoria type) {
        this.type = type;
    }

    public Categoria getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Categoria parentCategory) {
        this.parentCategory = parentCategory;
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
