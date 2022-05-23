package com.zokudo.sor.entities;

import com.zokudo.sor.enums.AccountCode;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "customer_kyc_limit", indexes = {
        @Index(name = "mobile_number", columnList = "mobile_number"),
        @Index(name = "kyc_type", columnList = "kyc_type")
})
public class CustomerKycLimit extends AbstractEntity {

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "kyc_type")
    @Enumerated(EnumType.STRING)
    private AccountCode kycType;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "balance_limit")
    private double balanceLimit;

    @Column(name = "monthly_limit")
    private double monthlyLimit;

    @Column(name = "yearly_limit")
    private double yearlyLimit;

    @Column(name = "remaining_balance_limit")
    private double remainingBalanceLimit;

    @Column(name = "remaining_monthly_limit")
    private double remainingMonthlyLimit;

    @Column(name = "remaining_yearly_limit")
    private double remainingYearlyLimit;

}
