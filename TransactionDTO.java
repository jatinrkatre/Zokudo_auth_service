package com.zokudo.sor.dto;

import java.util.Date;
import java.util.List;

import com.zokudo.sor.enums.TransactionTypes;
import lombok.Data;

@Data
public class TransactionDTO {

	private long id;
	private String clientName;
	private String agentHashId;
	private String agentCompany;
	private double billingAmt;
	private String cardType;
	private double cashBackAmt;
	private String entityName;
	private String comments;
	private Date createdAt;
	private double currentBalance;
	private String customerMobile;
	private String customerName;
	private String distributorHashId;	
	private String distributorCompany;
	private String email;
	private String maskCardNumber;
	private String merchantId;
	private String merchantName;
	private double originalAmount;
	private String originalTxnDate;
	private String pocketName;
	private String programName;
	private String proxyCardNo;
	private String retrivalRefNo;
	private String status;
	private double transactionAmt;
	private String transactionRefNo;
	private String transactionType;
	private Date updatedAt;
	private String walletHashId;
	private String agentName;
	private String distributorName;
	
	/* Transaction FETCH Request Body*/
	private String page;
	private String size;
	private String dateRange;
	private String mobileNumber;
	
	private Date startDate;
	private Date endDate;
	private Date previousDate;
	
	private String start;
	private String end;
	
	private String customerHashId;
	private List<TransactionTypes> tranasctionTypes;
	private String reconStatus;
	
	
	
	
	
}
