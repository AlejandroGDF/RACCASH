package com.raccooncash.api.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReporteFlujoCajaRespuesta {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netCashFlow;

    public ReporteFlujoCajaRespuesta() {
    }

    public ReporteFlujoCajaRespuesta(LocalDate from, LocalDate to, BigDecimal totalIncome, BigDecimal totalExpense) {
        this.from = from;
        this.to = to;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netCashFlow = totalIncome.subtract(totalExpense);
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getNetCashFlow() {
        return netCashFlow;
    }

    public void setNetCashFlow(BigDecimal netCashFlow) {
        this.netCashFlow = netCashFlow;
    }
}