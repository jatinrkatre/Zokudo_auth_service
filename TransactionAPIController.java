package com.zokudo.sor.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zokudo.sor.dto.TransactionDTO;
import com.zokudo.sor.entities.CustomerEODBalanceSumView;
import com.zokudo.sor.entities.CustomerEODBalanceView;
import com.zokudo.sor.entities.Transaction;
import com.zokudo.sor.exceptions.BizException;
import com.zokudo.sor.service.TransactionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("{programUrl}/api/v1/transaction")
public class TransactionAPIController {
	
	private final TransactionService transactionService;
	
	@Autowired
	public TransactionAPIController(final TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	
	@ApiOperation(value = "Transaction List In SOR", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/fetch",produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<Transaction> getTranasction(@RequestBody TransactionDTO dto){
		return transactionService.getTransaction(dto);
		
	}
	
	@ApiOperation(value = "Get Customer List", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/eod/balance" ,produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<CustomerEODBalanceView> getCustomerEODBalance(@RequestBody TransactionDTO dto){
		return transactionService.getCustomerEODBalance(dto);
	}
	
	@ApiOperation(value = "Get Customer List", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/eod/sum/balance" ,produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<CustomerEODBalanceSumView> getCustomerEODBalanceSum(@RequestBody TransactionDTO dto){
		return transactionService.getCustomerEODBalanceSum(dto);
	}
	
	@ApiOperation(value = "Set Customer Balance by program", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@GetMapping(value="/customerBalance")
	public void updateCustomerBalance(){
		transactionService.updateCustomerBalance();
	}
	
	
	@ApiOperation(value = "Download customer eod balance ", authorizations = {@Authorization(value = "basicAuth")})
	@CrossOrigin(allowCredentials = "true", allowedHeaders = "*", origins = {"*"}, methods = RequestMethod.GET)
	@GetMapping(value = "/downloadCustomerBalance", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void DownloadCustomerBalance(HttpServletRequest request, HttpServletResponse
			response, @PathVariable("programUrl") String programUrl,
									 @RequestParam final Map<String, String> requestParams) throws Exception {
		try {
			transactionService.downloadCustomerBalance(request, response, programUrl, requestParams);
		} catch (BizException e) {
			response.sendRedirect(request.getHeader("Referer"));
		}
	}
	
	@ApiOperation(value = "Download customer eod sum balance ", authorizations = {@Authorization(value = "basicAuth")})
	@CrossOrigin(allowCredentials = "true", allowedHeaders = "*", origins = {"*"}, methods = RequestMethod.GET)
	@GetMapping(value = "/downloadCustomerSumBalance", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void DownloadCustomerSumBalance(HttpServletRequest request, HttpServletResponse
			response, @PathVariable("programUrl") String programUrl,
									 @RequestParam final Map<String, String> requestParams) throws Exception {
		try {
			transactionService.downloadCustomerSumBalance(request, response, programUrl, requestParams);
		} catch (BizException e) {
			response.sendRedirect(request.getHeader("Referer"));
		}
	}


	@ApiOperation(value = "download txn view", authorizations = {@Authorization(value = "basicAuth")})
	@CrossOrigin(origins = {"*"}, allowCredentials = "true", allowedHeaders = "*", methods = RequestMethod.GET)
	@GetMapping(value = "/downloadTransaction", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void downloadGenericTransactionList(HttpServletRequest request, HttpServletResponse response, @RequestParam final Map<String, String> requestParams) throws Exception {
		try {
			transactionService.downloadTransactionReport(request, response, requestParams);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(request.getHeader("Referer"));
		}
	}


	// Get customer sum balance of by mobile - use only if scheduler fails
	@ApiOperation(value = "update customer balance by mobile number and program GPR or GC", authorizations = {@Authorization("basicAuth")})
	@CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@GetMapping(value="/updateCustomerBalanceSum")
	public void updateCustomerBalanceSum(){
		transactionService.updateCustomerBalanceSum();
	}
	
	@ApiOperation(value = "Get Transaction Details by date Range : Use only when cron fails", authorizations = {@Authorization("basicAuth")})
	@CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/updateTxnByDate")
	public ResponseEntity<?> updateCustomerTxnByDateRange(@RequestBody TransactionDTO dto){
		return transactionService.storeCustomerTransactionBasedOnDateRange(dto);
	}
	
	@ApiOperation(value = "Set EOD Balance by date Range : Use only when cron fails", authorizations = {@Authorization("basicAuth")})
	@CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/updateEODBalanceByDate")
	public ResponseEntity<?> updateEODBalanceByDate(@RequestBody TransactionDTO dto){
		return transactionService.setUpEODByDate(dto);
	}
	
	@ApiOperation(value = "Set Customer Balance By Program with date Range : Use only when cron fails", authorizations = {@Authorization("basicAuth")})
	@CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/updateCustomerBalanceByDate")
	public ResponseEntity<?> updateCustomerBalanceByDate(@RequestBody TransactionDTO dto){
		return transactionService.setUpCustomerBalanceByDate(dto);
	}
	
	@ApiOperation(value = "Set Customer Balance Sum By Program with date Range : Use only when cron fails", authorizations = {@Authorization("basicAuth")})
	@CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/updateCustomerBalanceSumByDate")
	public ResponseEntity<?> updateCustomerBalanceSumByDate(@RequestBody TransactionDTO dto){
		return transactionService.setUpCustomerBalanceSumByDate(dto);
	}
	
	

}
