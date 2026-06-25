package com.raccooncash.api.presupuesto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class LimiteCategoriaPresupuestoSolicitud {

    @NotNull(message = "La categoria es obligatoria")
    private Long categoryId;

    @NotNull(message = "El limite es obligatorio")
    @Positive(message = "El limite debe ser mayor a cero")
    private BigDecimal amountLimit;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }
}