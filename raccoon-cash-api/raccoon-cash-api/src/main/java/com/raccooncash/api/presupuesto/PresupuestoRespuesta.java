package com.raccooncash.api.presupuesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PresupuestoRespuesta {
    private Long id;
    private String nombre;
    private BigDecimal monto;
    private BigDecimal montoActual;
    private TipoPeriodoPresupuesto tipoPeriodo;
    private Integer valorPeriodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String color;
    private Boolean esGasto;
    private Boolean incluirTodasLasTransacciones;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public PresupuestoRespuesta() {
    }

    public PresupuestoRespuesta(Presupuesto budget, BigDecimal montoActual) {
        this.id = budget.getId();
        this.nombre = budget.getName();
        this.monto = budget.getAmountLimit();
        this.montoActual = montoActual;
        this.tipoPeriodo = budget.getPeriodType();
        this.valorPeriodo = budget.getPeriodValue();
        this.fechaInicio = budget.getStartDate();
        this.fechaFin = budget.getEndDate();
        this.color = budget.getColor();
        this.esGasto = budget.getExpense();
        this.incluirTodasLasTransacciones = budget.getIncludeAllTransactions();
        this.activo = budget.getActive();
        this.creadoEn = budget.getCreatedAt();
        this.actualizadoEn = budget.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMontoActual() {
        return montoActual;
    }

    public void setMontoActual(BigDecimal montoActual) {
        this.montoActual = montoActual;
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

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
