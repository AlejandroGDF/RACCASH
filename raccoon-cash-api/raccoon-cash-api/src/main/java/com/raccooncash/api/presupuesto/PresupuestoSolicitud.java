package com.raccooncash.api.presupuesto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class PresupuestoSolicitud {

    @NotBlank(message = "El nombre es obligatorio")
    @JsonAlias("name")
    private String nombre;

    @NotNull(message = "El monto del presupuesto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @JsonAlias("amountLimit")
    private Double monto;

    @NotNull(message = "El periodo es obligatorio")
    @JsonAlias("periodType")
    private TipoPeriodoPresupuesto tipoPeriodo;

    @NotNull(message = "El valor del periodo es obligatorio")
    @Positive(message = "El valor del periodo debe ser mayor a cero")
    private Integer valorPeriodo;

    @NotNull(message = "La fecha inicial es obligatoria")
    @JsonAlias("startDate")
    private LocalDate fechaInicio;

    @NotBlank(message = "El color es obligatorio")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "El color debe ser un valor hexadecimal valido")
    private String color;

    @NotNull(message = "El tipo de presupuesto es obligatorio")
    private Boolean esGasto;

    @NotNull(message = "Debe indicar si se incluyen todas las transacciones")
    private Boolean incluirTodasLasTransacciones;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public TipoPeriodoPresupuesto getTipoPeriodo() {
        return tipoPeriodo;
    }

    public void setTipoPeriodo(TipoPeriodoPresupuesto tipoPeriodo) {
        this.tipoPeriodo = tipoPeriodo;
    }

    public Integer getValorPeriodo() {
        return valorPeriodo;
    }

    public void setValorPeriodo(Integer valorPeriodo) {
        this.valorPeriodo = valorPeriodo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getEsGasto() {
        return esGasto;
    }

    public void setEsGasto(Boolean esGasto) {
        this.esGasto = esGasto;
    }

    public Boolean getIncluirTodasLasTransacciones() {
        return incluirTodasLasTransacciones;
    }

    public void setIncluirTodasLasTransacciones(Boolean incluirTodasLasTransacciones) {
        this.incluirTodasLasTransacciones = incluirTodasLasTransacciones;
    }
}
