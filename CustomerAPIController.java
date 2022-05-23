
package com.zokudo.sor.controller;

import com.zokudo.sor.dto.CustomerKycLimitBasedOnFiltersDTO;
import com.zokudo.sor.exceptions.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zokudo.sor.dto.CustomerDTO;
import com.zokudo.sor.entities.CustomerView;
import com.zokudo.sor.service.CustomerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("{programUrl}/api/v1/customer/")
public class CustomerAPIController {

	private final CustomerService customerService;

	@Autowired
	public CustomerAPIController(final CustomerService customerService) {
		this.customerService = customerService;
	}
	@PostMapping(value="/register")
	public ResponseEntity<?> createCustomer(@RequestBody CustomerDTO customerDTO){
		return customerService.registerCustomer(customerDTO);
	}
	
	@PostMapping(value="/update")
	public ResponseEntity<?> updateCustomer(@RequestBody CustomerDTO customerDTO){
		return customerService.updateCustomer(customerDTO);
	}
	
	@PostMapping(value="/updateKyc")
	public ResponseEntity<?> updateKycStatus(@RequestBody CustomerDTO customerDTO){
		return customerService.updateKycStatus(customerDTO);
	}

	@PostMapping(value="/updateKycLimit")
	public ResponseEntity<?>updateKycLimit(@RequestBody CustomerDTO customerDTO){
		return customerService.updateKycLimit(customerDTO);
	}
	
	@ApiOperation(value = "Get Customer List", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/fetch" ,produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<CustomerView> getCustomer(@RequestBody CustomerDTO customerDTO){
		return customerService.getCustomer(customerDTO);
	}


	@ApiOperation(value = "Download Customer List", authorizations = {@Authorization(value = "basicAuth")})
	@CrossOrigin(allowCredentials = "true", allowedHeaders = "*", origins = {"*"}, methods = RequestMethod.GET)
	@GetMapping(value = "/downloadCustomer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void DownloadCustomerList(HttpServletRequest request, HttpServletResponse
			response, @PathVariable("programUrl") String programUrl,
									 @RequestParam final Map<String, String> requestParams) throws Exception {
		try {
			customerService.downloadCustomerList(request, response, programUrl, requestParams);
		} catch (BizException e) {
			response.sendRedirect(request.getHeader("Referer"));
		}
	}
	
	@ApiOperation(value = "get customer by customerId", authorizations = {@Authorization(value = "basicAuth")})
	@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
	@GetMapping(value = "/kycLimitConfig")
	public ResponseEntity<?> kycLimitConfig() {
		return customerService.kycLimitConfig();
	}

	@ApiOperation(value = "Get Customer KYC Limit Based On Filters", authorizations = {@Authorization("basicAuth")})
	@CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value = "/getCustomerKycLimitListBasedOnfilters", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Object getCustomerKycLimitListBasedOnfilters(HttpServletRequest request,
											   @PathVariable("programUrl") String programUrl,
											   @RequestBody CustomerKycLimitBasedOnFiltersDTO customerKycLimitBasedOnFiltersDTO) {
		return customerService.execute(programUrl, customerKycLimitBasedOnFiltersDTO);
	}

	@ApiOperation(value = "download customer kyc limit view", authorizations = {@Authorization(value = "basicAuth")})
	@CrossOrigin(origins = {"*"}, allowCredentials = "true", allowedHeaders = "*", methods = RequestMethod.GET)
	@GetMapping(value = "/view/downloadCustomerKycLimitReport", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void downloadCustomerKycLimitReport(HttpServletRequest request, HttpServletResponse response,
											@PathVariable("programUrl") String programUrl,
											@RequestParam final Map<String, String> requestParams) throws Exception {
		try {
			customerService.downloadCustomerKycLimitReport(request, response, programUrl, requestParams);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(request.getHeader("Referer"));
		}
	}
}
