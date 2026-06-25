package com.raccooncash.api.deuda;

import com.raccooncash.api.cuenta.Cuenta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "debts", indexes = {
        @Index(name = "idx_debts_active", columnList = "active"),
        @Index(name = "idx_debts_type", columnList = "type"),
        @Index(name = "idx_debts_status", columnList = "status"),
        @Index(name = "idx_debts_due_date", columnList = "due_date"),
        @Index(name = "idx_debts_account_id", columnList = "account_id")
})
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String personName;

    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeuda type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDeuda status;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Cuenta account;

    private Boolean reminderEnabled = false;

    private LocalDateTime reminderAt;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (remainingAmount == null && totalAmount != null) {
            remainingAmount = totalAmount.subtract(paidAmount);
        }
        if (status == null) {
            status = EstadoDeuda.PENDING;
        }
        if (reminderEnabled == null) {
            reminderEnabled = false;
        }
        if (active == null) {
            active = true;
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

    public Cuenta getAccount() {
        return account;
    }

    public void setAccount(Cuenta account) {
        this.account = account;
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
