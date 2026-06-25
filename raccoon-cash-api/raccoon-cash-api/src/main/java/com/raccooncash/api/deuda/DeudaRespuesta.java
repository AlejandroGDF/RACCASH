package com.raccooncash.api.deuda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeudaRespuesta {
    private Long id;
    private String personName;
    private String description;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private TipoDeuda type;
    private EstadoDeuda status;
    private LocalDate dueDate;
    private Boolean overdue;
    private Long accountId;
    private String accountName;
    private Boolean reminderEnabled;
    private LocalDateTime reminderAt;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeudaRespuesta() {
    }

    public DeudaRespuesta(Deuda debt) {
        this.id = debt.getId();
        this.personName = debt.getPersonName();
        this.description = debt.getDescription();
        this.totalAmount = debt.getTotalAmount();
        this.paidAmount = debt.getPaidAmount();
        this.remainingAmount = debt.getRemainingAmount();
        this.type = debt.getType();
        this.status = debt.getStatus();
        this.dueDate = debt.getDueDate();
        this.overdue = debt.getDueDate() != null
                && debt.getDueDate().isBefore(LocalDate.now())
                && debt.getStatus() != EstadoDeuda.PAID
                && debt.getStatus() != EstadoDeuda.CANCELLED;
        this.accountId = debt.getAccount().getId();
        this.accountName = debt.getAccount().getName();
        this.reminderEnabled = Boolean.TRUE.equals(debt.getReminderEnabled());
        this.reminderAt = debt.getReminderAt();
        this.active = debt.getActive();
        this.createdAt = debt.getCreatedAt();
        this.updatedAt = debt.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public TipoDeuda getType() {
        return type;
    }

    public void setType(TipoDeuda type) {
        this.type = type;
    }

    public EstadoDeuda getStatus() {
        return status;
    }

    public void setStatus(EstadoDeuda status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
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

    public Boolean getReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(Boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public LocalDateTime getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(LocalDateTime reminderAt) {
        this.reminderAt = reminderAt;
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
