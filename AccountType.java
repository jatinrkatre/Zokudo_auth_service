package com.zokudo.sor.entities;

import com.zokudo.sor.enums.AccountCode;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "account_type", indexes = {
        @Index(name = "code", columnList = "code"),
})
public class AccountType extends AbstractEntity {

    @Column(name = "balance_limit", nullable = false)
    double balanceLimit;

    @Column(name = "code", nullable = false)
    @Enumerated(EnumType.STRING)
    AccountCode code;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "daily_limit", nullable = false)
    double dailyLimit;

    @Column(name = "monthly_limit", nullable = false)
    double monthlyLimit;

    @Column(name = "yearly_limit", nullable = false)
    double yearlyLimit;

    @Column(name = "transaction_limit", nullable = false)
    double transactionLimit;

    @Column(name = "description", nullable = false)
    String description;

    @Transient
//    final static short DEFAULT_ACCOUNT_TYPE_ID = 1;
    final static short MIN_KYC_ACCOUNT_TYPE_ID = 1;
    final static short FULL_KYC_ACCOUNT_TYPE_ID = 2;

//    public static AccountType getDefault() {
//        AccountType accountType = new AccountType();
//        accountType.setId(DEFAULT_ACCOUNT_TYPE_ID);
//        return accountType;
//    }

    public static AccountType getMinKYC() {
        AccountType accountType = new AccountType();
        accountType.setId(MIN_KYC_ACCOUNT_TYPE_ID);
        return accountType;
    }

    public static AccountType getFullKYC() {
        AccountType accountType = new AccountType();
        accountType.setId(FULL_KYC_ACCOUNT_TYPE_ID);
        return accountType;
    }

    public double getBalanceLimit() {
        return balanceLimit;
    }

    public void setBalanceLimit(double balanceLimit) {
        this.balanceLimit = balanceLimit;
    }

    public AccountCode getCode() {
        return code;
    }

    public void setCode(AccountCode code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(double dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getYearlyLimit() {
        return yearlyLimit;
    }

    public void setYearlyLimit(double yearlyLimit) {
        this.yearlyLimit = yearlyLimit;
    }

    public double getTransactionLimit() {
        return transactionLimit;
    }

    public void setTransactionLimit(double transactionLimit) {
        this.transactionLimit = transactionLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
