package com.raccooncash.api.panel;

import java.math.BigDecimal;

public class ResumenPanelRespuesta {
    private BigDecimal totalBalance;
    private BigDecimal totalIncomeThisMonth;
    private BigDecimal totalExpenseThisMonth;
    private BigDecimal netCashFlowThisMonth;
    private BigDecimal totalDebtsIOwe;
    private BigDecimal totalDebtsOwedToMe;
    private long numberOfAccounts;
    private long numberOfActiveBudgets;

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getTotalIncomeThisMonth() {
        return totalIncomeThisMonth;
    }

    public void setTotalIncomeThisMonth(BigDecimal totalIncomeThisMonth) {
        this.totalIncomeThisMonth = totalIncomeThisMonth;
    }

    public BigDecimal getTotalExpenseThisMonth() {
        return totalExpenseThisMonth;
    }

    public void setTotalExpenseThisMonth(BigDecimal totalExpenseThisMonth) {
        this.totalExpenseThisMonth = totalExpenseThisMonth;
    }

    public BigDecimal getNetCashFlowThisMonth() {
        return netCashFlowThisMonth;
    }

    public void setNetCashFlowThisMonth(BigDecimal netCashFlowThisMonth) {
        this.netCashFlowThisMonth = netCashFlowThisMonth;
    }

    public BigDecimal getTotalDebtsIOwe() {
        return totalDebtsIOwe;
    }

    public void setTotalDebtsIOwe(BigDecimal totalDebtsIOwe) {
        this.totalDebtsIOwe = totalDebtsIOwe;
    }

    public BigDecimal getTotalDebtsOwedToMe() {
        return totalDebtsOwedToMe;
    }

    public void setTotalDebtsOwedToMe(BigDecimal totalDebtsOwedToMe) {
        this.totalDebtsOwedToMe = totalDebtsOwedToMe;
    }

    public long getNumberOfAccounts() {
        return numberOfAccounts;
    }

    public void setNumberOfAccounts(long numberOfAccounts) {
        this.numberOfAccounts = numberOfAccounts;
    }

    public long getNumberOfActiveBudgets() {
        return numberOfActiveBudgets;
    }

    public void setNumberOfActiveBudgets(long numberOfActiveBudgets) {
        this.numberOfActiveBudgets = numberOfActiveBudgets;
    }
}