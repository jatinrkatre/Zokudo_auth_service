package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import com.zokudo.sor.enums.CardStatus;
import com.zokudo.sor.enums.CardType;
import com.zokudo.sor.enums.ProgramPlans;

import lombok.Data;

@Data
@Entity
@Table(name = "cards")
public class Cards extends AbstractEntity {

    @Column(name = "card_status")
    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    @Column(name = "card_activation_status")
    @Enumerated(EnumType.STRING)
    private CardStatus cardActivationStatus;

    @Column(name = "proxy_number",unique=true)
    private String proxyNumber;

    @Column(name = "activation_code")
    private String activationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType;

    @Column(name = "mask_card_number")
    private String maskCardNumber;

    @Column(name = "card_hash_id")
    private String cardHashId;

    @Column(name = "wallet_id")
    private long walletId;

    @Column(name = "customer_hash_id")
    private String customerHashId;

    @Column(name = "program_hash_id")
    private String programHashId;

    @Column(name = "program_plan")
    @Enumerated(EnumType.STRING)
    private ProgramPlans programPlan;

    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "client_id")
    private long clientId;

    @Column(name = "fee_deduction", nullable = false)
    private boolean feeDeduction = false;
    
/*    @Column(name = "program_name")
    private String programName;

    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "customer_mobile")
    private String customerMobile;
*/
    
    @Column(name="program_id")
    private long programId;
    
    @Column(name="org_created_at")
    private Date orgCreatedAt;
   
}
