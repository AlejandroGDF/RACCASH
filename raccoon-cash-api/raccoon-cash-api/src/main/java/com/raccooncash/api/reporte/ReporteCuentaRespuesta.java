package com.raccooncash.api.reporte;

import java.math.BigDecimal;

public class ReporteCuentaRespuesta {
    private Long accountId;
    private String accountName;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal totalTransferIn = BigDecimal.ZERO;
    private BigDecimal totalTransferOut = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;

    public ReporteCuentaRespuesta() {
    }

    public ReporteCuentaRespuesta(Long accountId, String accountName) {
        this.accountId = accountId;
        this.accountName = accountName;
    }

    public void addIncome(BigDecimal amount) {
        totalIncome = totalIncome.add(amount);
        netAmount = netAmount.add(amount);
    }

    public void addExpense(BigDecimal amount) {
        totalExpense = totalExpense.add(amount);
        netAmount = netAmount.subtract(amount);
    }

    public void addTransferIn(BigDecimal amount) {
        totalTransferIn = totalTransferIn.add(amount);
        netAmount = netAmount.add(amount);
    }

    public void addTransferOut(BigDecimal amount) {
        totalTransferOut = totalTransferOut.add(amount);
        netAmount = netAmount.subtract(amount);
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

    public BigDecimal getTotalTransferIn() {
        return totalTransferIn;
    }

    public void setTotalTransferIn(BigDecimal totalTransferIn) {
        this.totalTransferIn = totalTransferIn;
    }

    public BigDecimal getTotalTransferOut() {
        return totalTransferOut;
    }

    public void setTotalTransferOut(BigDecimal totalTransferOut) {
        this.totalTransferOut = totalTransferOut;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
}