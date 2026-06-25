package com.raccooncash.api.savinggoal;

import com.raccooncash.api.transaccion.Transaccion;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
public class SavingGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double targetAmount;
    private LocalDate deadline;
    private String color;
    private String icon;
    private String currency;

    @OneToMany(mappedBy = "savingGoal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaccion> transactions;

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

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Set<Transaccion> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaccion> transactions) {
        this.transactions = transactions;
    }
}
