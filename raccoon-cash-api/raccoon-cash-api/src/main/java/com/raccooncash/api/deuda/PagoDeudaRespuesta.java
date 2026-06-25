package com.raccooncash.api.deuda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PagoDeudaRespuesta {
    private Long id;
    private Long debtId;
    private Long accountId;
    private String accountName;
    private Long transactionId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String notes;
    private Boolean active;
    private LocalDateTime createdAt;

    public PagoDeudaRespuesta() {
    }

    public PagoDeudaRespuesta(PagoDeuda payment) {
        this.id = payment.getId();
        this.debtId = payment.getDebt().getId();
        if (payment.getAccount() != null) {
            this.accountId = payment.getAccount().getId();
            this.accountName = payment.getAccount().getName();
        }
        if (payment.getTransaction() != null) {
            this.transactionId = payment.getTransaction().getId();
        }
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
        this.notes = payment.getNotes();
        this.active = payment.getActive();
        this.createdAt = payment.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDebtId() {
        return debtId;
    }

    public void setDebtId(Long debtId) {
        this.debtId = debtId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
