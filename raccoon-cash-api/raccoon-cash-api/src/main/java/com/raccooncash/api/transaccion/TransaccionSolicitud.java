package com.raccooncash.api.transaccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class TransaccionSolicitud {
    @NotBlank(message = "La descripcion es obligatoria")
    private String description;
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal amount;
    private LocalDateTime date;
    @NotNull(message = "El tipo de transaccion es obligatorio")
    private TipoTransaccion type;
    @NotNull(message = "La cuenta es obligatoria")
    private Long accountId;
    private Long destinationAccountId;
    private Long toAccountId;
    private Long categoryId;
    private Long savingGoalId;
    private String notes;
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
    public Long getAccountId() {
        return accountId;
    }
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    public Long getDestinationAccountId() {
        return destinationAccountId;
    }
    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
    public Long getToAccountId() {
        return toAccountId;
    }
    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }
    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public Long getSavingGoalId() {
        return savingGoalId;
    }
    public void setSavingGoalId(Long savingGoalId) {
        this.savingGoalId = savingGoalId;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public Long getResolvedDestinationAccountId() {
        return destinationAccountId != null ? destinationAccountId : toAccountId;
    }
}
