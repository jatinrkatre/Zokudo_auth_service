package com.zokudo.sor.dto;

import java.util.Date;

import com.zokudo.sor.enums.CardStatus;
import com.zokudo.sor.enums.CardType;
import com.zokudo.sor.enums.ProgramPlans;

import lombok.Data;

@Data
public class CardDTO {

	private long id;
	private Date createdAt;
	private Date updatedAt;
	private CardStatus cardStatus;
	private CardStatus cardActivationStatus;
	private String activationCode;
	private long walletId;
	private String proxyNumber;
	private CardType cardType;
	private String maskCardNumber;
	private String cardHashId;
	private String customerHashId;
	private String cardImageUrl;
	private String zaggleCardId;
	private String zaggleProductCode;
	private long programId;
	private ProgramPlans programPlan;
	private String expiryDate;
	private String remarks;
	private long clientId;
	private String programName;
	private String customerName;
	private String customerMobile;
	private String agentHashId;
	private String distributorHashId;
	private boolean feeDeduction; 
	private double balance;
	
	// UI Request 
	private String dateRange;
	private String page;
	private String size;
	private Date startDate;
	private Date endDate;
	private String mobile;
	private String cardTypeFilter;
	private String programPlanFilter;
	private String status;
}
