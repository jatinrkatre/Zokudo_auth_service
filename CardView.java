package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import com.zokudo.sor.enums.Status;
import org.hibernate.annotations.Immutable;

import com.zokudo.sor.enums.CardStatus;
import com.zokudo.sor.enums.CardType;
import com.zokudo.sor.enums.ProgramPlans;

import lombok.Data;

@Entity
@Immutable
@Data
public class CardView {

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="created_at")
	private Date createdAt;
	
	@Column(name="updated_at")
	private Date updatedAt;
	
	@Column(name="activation_code")
	private String activationCode;
	
	@Column(name = "card_activation_status")
	@Enumerated(EnumType.STRING)
	private CardStatus cardActivationStatus;

	@Column(name = "proxy_number")
	private String proxyNumber;


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

	@Column(name = "fee_deduction")
	private boolean feeDeduction = false;

	@Column(name = "program_name")
	private String programName;
	
	@Column(name = "customer_name")
	private String customerName;
	
	@Column(name = "mobile")
	private String mobile;

	@Column(name = "card_status")
	@Enumerated(EnumType.STRING)
	private Status cardStatus;

	@Column(name="org_created_at")
	private Date orgCreatedAt;
	
}
