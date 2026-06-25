package com.raccooncash.api.cuenta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CuentaSolicitud {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private TipoCuenta type;

    @NotNull(message = "El balance inicial es obligatorio")
    private BigDecimal initialBalance;

    @NotBlank(message = "La moneda es obligatoria")
    private String currency;

    private Integer decimalPrecision;

    private String color;

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
}
