package com.raccooncash.api.cuenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CuentaRespuesta {
    private Long id;
    private String name;
    private TipoCuenta type;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private String currency;
    private Integer decimalPrecision;
    private String color;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CuentaRespuesta() {
    }

    public CuentaRespuesta(Cuenta account) {
        this.id = account.getId();
        this.name = account.getName();
        this.type = account.getType();
        this.initialBalance = account.getInitialBalance();
        this.currentBalance = account.getCurrentBalance();
        this.currency = account.getCurrency();
        this.decimalPrecision = account.getDecimalPrecision();
        this.color = account.getColor();
        this.active = account.getActive();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
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

    public TipoCuenta getType() {
        return type;
    }

    public void setType(TipoCuenta type) {
        this.type = type;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getDecimalPrecision() {
        return decimalPrecision;
    }

    public void setDecimalPrecision(Integer decimalPrecision) {
        this.decimalPrecision = decimalPrecision;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
