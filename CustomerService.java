package com.zokudo.sor.service;

import com.zokudo.sor.dto.CustomerKycLimitBasedOnFiltersDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.zokudo.sor.dto.CustomerDTO;
import com.zokudo.sor.entities.Customer;
import com.zokudo.sor.entities.CustomerView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface CustomerService {

	public ResponseEntity<?> registerCustomer(CustomerDTO dto);
	
	public ResponseEntity<?> updateCustomer(CustomerDTO dto);
	
	public ResponseEntity<?> updateKycStatus(CustomerDTO dto);
	
	public Page<CustomerView> getCustomer(CustomerDTO dto);
	
	public void storeCustomerDetails();

	void downloadCustomerList(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams) throws Exception;

    ResponseEntity<?> kycLimitConfig();

    Object execute(String programUrl, CustomerKycLimitBasedOnFiltersDTO customerKycLimitBasedOnFiltersDTO);

    void downloadCustomerKycLimitReport(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams);

    void kycLimitConfigByCron();

    public ResponseEntity<?> updateKycLimit(CustomerDTO dto);
}
