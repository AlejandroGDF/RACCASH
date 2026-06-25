package com.raccooncash.api.presupuesto;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal amountLimit;

    @Column(nullable = false)
    @Convert(converter = TipoPeriodoPresupuestoConvertidor.class)
    private TipoPeriodoPresupuesto periodType;

    private Integer periodValue;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean active = true;

    private String color = "#22C55E";

    private Boolean expense = true;

    private Boolean includeAllTransactions = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (periodValue == null) {
            periodValue = 1;
        }
        if (color == null) {
            color = "#22C55E";
        }
        if (expense == null) {
            expense = true;
        }
        if (includeAllTransactions == null) {
            includeAllTransactions = true;
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

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public TipoPeriodoPresupuesto getPeriodType() {
        return periodType;
    }

    public void setPeriodType(TipoPeriodoPresupuesto periodType) {
        this.periodType = periodType;
    }

    public Integer getPeriodValue() {
        return periodValue != null ? periodValue : 1;
    }

    public void setPeriodValue(Integer periodValue) {
        this.periodValue = periodValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getColor() {
        return color != null ? color : "#22C55E";
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getExpense() {
        return expense != null ? expense : true;
    }

    public void setExpense(Boolean expense) {
        this.expense = expense;
    }

    public Boolean getIncludeAllTransactions() {
        return includeAllTransactions != null ? includeAllTransactions : true;
    }

    public void setIncludeAllTransactions(Boolean includeAllTransactions) {
        this.includeAllTransactions = includeAllTransactions;
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
