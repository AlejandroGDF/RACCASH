package com.raccooncash.api.savinggoal;

import java.time.LocalDate;

public class SavingGoalResponse {
    private Long id;
    private String name;
    private Double targetAmount;
    private Double currentAmount;
    private LocalDate deadline;
    private String color;
    private String icon;
    private String currency;
    private int transactionCount;

    // Constructor
    public SavingGoalResponse(Long id, String name, Double targetAmount, Double currentAmount, LocalDate deadline, String color, String icon, String currency, int transactionCount) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.color = color;
        this.icon = icon;
        this.currency = currency;
        this.transactionCount = transactionCount;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public Double getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public String getCurrency() {
        return currency;
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}
