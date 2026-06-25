package com.raccooncash.api.transaccion;
import com.raccooncash.api.cuenta.Cuenta;
import com.raccooncash.api.categoria.Categoria;
import com.raccooncash.api.savinggoal.SavingGoal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity(name = "TransaccionFinanciera")
@Table(name = "transactions")
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransaccion type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Cuenta account;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Cuenta toAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Categoria category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_goal_id")
    private SavingGoal savingGoal;
    private String notes;
    private Boolean active = true;
    private Boolean generatedByDebtPayment = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
        if (generatedByDebtPayment == null) {
            generatedByDebtPayment = false;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public TipoTransaccion getType() {
        return type;
    }
    public void setType(TipoTransaccion type) {
        this.type = type;
    }
    public Cuenta getAccount() {
        return account;
    }
    public void setAccount(Cuenta account) {
        this.account = account;
    }
    public Cuenta getToAccount() {
        return toAccount;
    }
    public void setToAccount(Cuenta toAccount) {
        this.toAccount = toAccount;
    }
    public Categoria getCategory() {
        return category;
    }
    public void setCategory(Categoria category) {
        this.category = category;
    }
    public SavingGoal getSavingGoal() {
        return savingGoal;
    }
    public void setSavingGoal(SavingGoal savingGoal) {
        this.savingGoal = savingGoal;
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
    public Boolean getGeneratedByDebtPayment() {
        return generatedByDebtPayment;
    }
    public void setGeneratedByDebtPayment(Boolean generatedByDebtPayment) {
        this.generatedByDebtPayment = generatedByDebtPayment;
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
