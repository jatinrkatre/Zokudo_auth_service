package com.zokudo.sor.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zokudo.sor.enums.TransactionTypes;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.zokudo.sor.dto.TransactionDTO;
import com.zokudo.sor.entities.CronReport;
import com.zokudo.sor.entities.CustomerEODBalanceSumView;
import com.zokudo.sor.entities.CustomerEODBalanceView;
import com.zokudo.sor.entities.Transaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface TransactionService {

	public void storeCustomerTransaction();
	
	public Page<Transaction> getTransaction(TransactionDTO dto);
	
	public void setUpEODBalanceIncremental();
	
	public Page<CustomerEODBalanceView> getCustomerEODBalance(TransactionDTO dto);
	
	public Page<CustomerEODBalanceSumView> getCustomerEODBalanceSum(TransactionDTO dto);
	
	public void updateCustomerBalance();
	
	public void updateCustomerBalanceSum();

	public void downloadTransactionReport(HttpServletRequest request, HttpServletResponse response,Map<String, String> requestParams);

	void downloadCustomerBalance(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams) throws Exception;
	
	void downloadCustomerSumBalance(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams) throws Exception;
	
	Double getAnnualWalletCreditBalanceByCustomerForGPR(String customerMobile, TransactionTypes transactionType);

	Double getMonthlyWalletCreditBalanceByCustomerForGPR(String customerMobile, TransactionTypes transactionType);

	Double getCurrentBalanceByCustomerForGPR(String customerMob);

    List<String> getDistinctMobileListForGPR();
    
    public ResponseEntity<?> storeCustomerTransactionBasedOnDateRange(TransactionDTO dto);

    public ResponseEntity<?> setUpEODByDate(TransactionDTO dto);
    
    public ResponseEntity<?> setUpCustomerBalanceByDate(TransactionDTO dto);
    
    public ResponseEntity<?> setUpCustomerBalanceSumByDate(TransactionDTO dto);
}
