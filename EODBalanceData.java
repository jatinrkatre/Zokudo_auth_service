package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Entity
@Table(name = "eod_balance_data", indexes = {
        @Index(name = "proxy_card_no", columnList = "proxy_card_no")
})
public class EODBalanceData extends AbstractEntity {


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private Date transactionDate;

    @Column(name = "program_name", nullable = false)
    String programName;

    @Column(name = "client_name", nullable = false)
    String clientName;

    @Column(name = "proxy_card_no", nullable = false)
    String proxyCardNo;

    @Column(name = "card_type", nullable = false)
    String cardType;

    @Column(name = "expired", nullable = false)
    String expired;

    @Column(name = "current_balance", nullable = false)
    double currentBalance;
    
    @Column(name="customer_hash_id")
    private String customerHashId;
    
    @Column(name="customer_name")
    private String customerName;
    
    @Column(name="customer_mobile")
    private String customerMobile;
}
