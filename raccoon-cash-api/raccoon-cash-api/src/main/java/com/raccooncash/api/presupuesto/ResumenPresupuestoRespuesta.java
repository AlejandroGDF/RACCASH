package com.raccooncash.api.presupuesto;

import java.math.BigDecimal;

public class ResumenPresupuestoRespuesta {
    private String budgetName;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal percentageUsed;

    public ResumenPresupuestoRespuesta() {
    }

    public ResumenPresupuestoRespuesta(String budgetName, BigDecimal limit, BigDecimal spent, BigDecimal remaining, BigDecimal percentageUsed) {
        this.budgetName = budgetName;
        this.limit = limit;
        this.spent = spent;
        this.remaining = remaining;
        this.percentageUsed = percentageUsed;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void setRemaining(BigDecimal remaining) {
        this.remaining = remaining;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(BigDecimal percentageUsed) {
        this.percentageUsed = percentageUsed;
    }
}