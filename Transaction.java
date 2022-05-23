package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "transaction")
public class Transaction extends AbstractEntity{

	private long id;
	
	@Column(name="transaction_id",unique = true)
	private long transactionId;
	
	@Column(name = "client_name")
	private String clientName;
	
	@Column(name = "agent_hash_id")
	private String agentHashId;
	
	@Column(name = "agent_company")
	private String agentCompany;
	
	@Column(name = "billing_amt")
	private double billingAmt;
	
	@Column(name = "card_type")
	private String cardType;
	
	@Column(name = "cashback_amt")
	private double cashBackAmt;
	
	@Column(name = "entity_name")
	private String entityName;
	
	@Column(name = "comments")
	private String comments;
	
	@Column(name = "created_at")
	private Date createdAt;
	
	@Column(name = "current_balance")
	private double currentBalance;
	
	@Column(name = "customer_mobile")
	private String customerMobile;
	
	@Column(name = "customer_name")
	private String customerName;
	
	@Column(name = "distributor_hash_id")
	private String distributorHashId;	
	
	@Column(name = "distributor_company")
	private String distributorCompany;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "mask_card_number")
	private String maskCardNumber;
	
	@Column(name = "merchant_id")
	private String merchantId;
	
	@Column(name = "merchant_name")
	private String merchantName;
	
	@Column(name = "original_amount")
	private double originalAmount;
	
	@Column(name = "original_txn_date")
	private String originalTxnDate;
	
	@Column(name = "pocket_name")
	private String pocketName;
	
	@Column(name = "program_name")
	private String programName;
	
	@Column(name = "proxy_card_no")
	private String proxyCardNo;
	
	@Column(name = "retrival_ref_no")
	private String retrivalRefNo;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "transaction_amt")
	private double transactionAmt;
	
	@Column(name = "transaction_ref_no")
	private String transactionRefNo;
	
	@Column(name = "transaction_type")
	private String transactionType;
	
	@Column(name = "updated_at")
	private Date updatedAt;
	
	@Column(name = "wallet_hash_id")
	private String walletHashId;
	
	@Column(name = "agent_name")
	private String agentName;
	
	@Column(name = "distributor_name")
	private String distributorName;
	
	@Column(name="txn_created_at")
	private Date transactionCreatedAt;
	
	@Column(name="customer_hash_id")
	private String customerHashId;
	
	
}
