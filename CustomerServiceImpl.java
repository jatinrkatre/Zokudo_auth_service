package com.zokudo.sor.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zokudo.sor.dto.*;
import com.zokudo.sor.entities.AccountType;
import com.zokudo.sor.entities.Customer;
import com.zokudo.sor.entities.CustomerKycLimit;
import com.zokudo.sor.entities.CustomerView;
import com.zokudo.sor.enums.*;
import com.zokudo.sor.exceptions.BizException;
import com.zokudo.sor.repositories.*;
import com.zokudo.sor.service.CustomerService;
import com.zokudo.sor.service.TransactionService;
import com.zokudo.sor.util.CommonUtil;
import com.zokudo.sor.util.MonetaryUtil;
import com.zokudo.sor.util.SecurityUtil;
import com.zokudo.sor.util.UrlMetaData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.zokudo.sor.dto.ApiError;
import com.zokudo.sor.dto.CustomerDTO;
import com.zokudo.sor.repositories.CustomerRepository;
import com.zokudo.sor.repositories.CustomerViewRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final long MIN_KYC = 1;
    private final long FULL_KYC = 2;

    private final CustomerRepository customerRepository;
    private Client client;
    private final UrlMetaData urlMetaData;
    private final String applicationLevelUserName;
    private final String applicationLevelUserPassword;
    private static long recordCount = 0l;
    private final CustomerViewRepository customerViewRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final TransactionService transactionService;
    private final CustomerKycLimitRepository customerKycLimitRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public CustomerServiceImpl(final Client client, final UrlMetaData urlMetaData,
                               @Value("${applicationLevel.user.name}") String applicationLevelUserName,
                               @Value("${applicationLevel.user.password}") String applicationLevelUserPassword,
                               final CustomerRepository customerRepository,
                               final CustomerViewRepository customerViewRepository,
                               final AccountTypeRepository accountTypeRepository,
                               final TransactionService transactionService,
                               final CustomerKycLimitRepository customerKycLimitRepository, SecurityUtil securityUtil) {

        this.client = client;
        this.urlMetaData = urlMetaData;
        this.applicationLevelUserName = applicationLevelUserName;
        this.applicationLevelUserPassword = applicationLevelUserPassword;
        this.customerRepository = customerRepository;
        this.customerViewRepository = customerViewRepository;
        this.accountTypeRepository = accountTypeRepository;
        this.transactionService = transactionService;
        this.customerKycLimitRepository = customerKycLimitRepository;
        this.securityUtil = securityUtil;
    }

    @Override
    public void storeCustomerDetails() {
        try {
            long begin = System.currentTimeMillis();
            recordCount = 0l;
            Date previousDate = CommonUtil.getPastDateFromCurrentDate();
            String startDate = CommonUtil.getQuarterStartTime(Quater.FIRST_QUARTER, previousDate);
            String endDate = CommonUtil.getQuarterEndTime(Quater.FIRST_QUARTER, previousDate);
            log.info("** Fetching customer details for Quarter 1, start {} end {} ", startDate, endDate);
            getCustomerDetailsByDate(startDate, endDate);
            Thread.sleep(5000);

            startDate = CommonUtil.getQuarterStartTime(Quater.SECOND_QUARTER, previousDate);
            endDate = CommonUtil.getQuarterEndTime(Quater.SECOND_QUARTER, previousDate);
            log.info("** Fetching customer details for Quarter 2, start  {} end  {} ", startDate, endDate);
            getCustomerDetailsByDate(startDate, endDate);
            Thread.sleep(5000);

            startDate = CommonUtil.getQuarterStartTime(Quater.THIRD_QUARTER, previousDate);
            endDate = CommonUtil.getQuarterEndTime(Quater.THIRD_QUARTER, previousDate);
            log.info("** Fetching customer details for Quarter 3, start  {} end  {} ", startDate, endDate);
            getCustomerDetailsByDate(startDate, endDate);
            Thread.sleep(5000);

            startDate = CommonUtil.getQuarterStartTime(Quater.FOURTH_QUARTER, previousDate);
            endDate = CommonUtil.getQuarterEndTime(Quater.FOURTH_QUARTER, previousDate);
            log.info("** Fetching customer details for Quarter 4, start  {} end  {} ", startDate, endDate);
            getCustomerDetailsByDate(startDate, endDate);
            Thread.sleep(5000);

            long endTime = System.currentTimeMillis();
            double time = (endTime - begin) / 1000;
            log.info("** Time Required to fetch {} records is: {}s ", recordCount, time);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while storing cutomer txn. ", e.getMessage());
        }
    }

    @Override
    public void downloadCustomerList(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams) throws Exception {
        final String role = requestParams.get("role");
        CustomerDTO dto = new CustomerDTO();
        dto.setContactNo(requestParams.get("mobileNo"));
        dto.setDateRange(requestParams.get("dateRange"));
        dto.setProgramName(requestParams.get("programName"));
        dto.setCustomerHashId(requestParams.get("customerHashId"));
        Date startDate = null, endDate = null;
        if (StringUtils.isNotBlank(dto.getDateRange())) {

            String dateArray[] = null;
            dateArray = dto.getDateRange().split("-");

            String startDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dateArray[0] + CommonUtil.startTime));
            startDate = new SimpleDateFormat("yyyy-MM-dd ").parse(startDate1);

            String endtDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dateArray[1] + CommonUtil.endTime));
            endDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endtDate1);
            dto.setStartDate(startDate);
            dto.setEndDate(endDate);
        }
        final List<CustomerView> customerList = customerViewRepository.findAll(customerSpecification(dto));
        if (customerList instanceof List<?>) {
            String resultantFileName = "Customer_List.xlsx";

            if (customerList.size() == 0) {
                throw new BizException("No Customer data found!");
            }
            final List<Map<String, String>> resultantData = new ArrayList<Map<String, String>>();
            final List<String> headers = new ArrayList<String>();
            headers.add("Created At");
            headers.add("Customer Name");
            headers.add("Mobile");
            headers.add("Customer Hash ID");
            headers.add("Program Name");
            headers.add("Program Type");
            headers.add("ID Type");
            headers.add("ID Number");
            headers.add("ID Expiry Date");
            headers.add("Kyc Type");
            headers.add("Email Address");


            for (CustomerView customerData : customerList) {
                final Map<String, String> dataMap = new HashMap<String, String>();
                final String originalCreatedDate = StringUtils.isNotEmpty(String.valueOf(customerData.getOriginalCreatedAt())) ? String.valueOf(customerData.getOriginalCreatedAt()) : "NA";
                final String customerName = StringUtils.isNotEmpty(customerData.getCustomerName()) ? customerData.getCustomerName() : "NA";
                final String mobile = StringUtils.isNotEmpty(customerData.getMobile()) ? customerData.getMobile() : "NA";
                final String customerHashId = StringUtils.isNotEmpty(customerData.getCustomerHashId()) ? customerData.getCustomerHashId() : "NA";
                final String ProgramName = StringUtils.isNotEmpty(customerData.getProgramName()) ? customerData.getProgramName() : "NA";
                final String idType = StringUtils.isNotEmpty(customerData.getIdType()) ? customerData.getIdType() : "NA";
                final String idNumber = StringUtils.isNotEmpty(customerData.getIdNumber()) ? customerData.getIdNumber() : "NA";
                final String idExpiryDate = String.valueOf(customerData.getIdExpiryDate());
                final String kycType = StringUtils.isNotEmpty(String.valueOf(customerData.getKycType())) ? String.valueOf(customerData.getKycType()) : "NA";
                final String email = StringUtils.isNotEmpty(customerData.getEmailAddress()) ? customerData.getEmailAddress() : "NA";
                final String programType = StringUtils.isNotEmpty(customerData.getProgramType()) ? customerData.getProgramType() : "NA";

                dataMap.put("Created At", originalCreatedDate);
                dataMap.put("Customer Name", customerName);
                dataMap.put("Mobile", mobile);
                dataMap.put("Customer Hash ID", customerHashId);
                dataMap.put("Program Name", ProgramName);
                dataMap.put("Program Type", programType);
                dataMap.put("ID Type", idType);
                dataMap.put("ID Number", idNumber);
                dataMap.put("ID Expiry Date", idExpiryDate);
                dataMap.put("Kyc Type", kycType);
                dataMap.put("Email Address", email);


                resultantData.add(dataMap);
            }
            CommonUtil.generateExcelSheet(headers, resultantData, resultantFileName, response);
        }
    }

    public void getCustomerDetailsByDate(String startDate, String endDate) {

        final JSONObject requestParameters = new JSONObject();
        try {
            requestParameters.put("startDate", startDate);
            requestParameters.put("endDate", endDate);
            final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
            headerMap.add("Authorization", securityUtil.getAuthorizationHeader());
            headerMap.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            String strURl = urlMetaData.GET_CUSTOMERS_DETAILS_INCREMENTAL;

            Response clientResponse = client.target(strURl)
                    .request()
                    .headers(headerMap)
                    .post(Entity.entity(requestParameters.toString(), MediaType.APPLICATION_JSON_VALUE));

            String customerDetailsObj = clientResponse.readEntity(String.class);
            if (clientResponse.getStatus() != 200)
                throw new BizException(BizErrors.DATA_NOT_FOUND.getValue(), "unable to fetch Customer Details");
            JSONArray customerObj = new JSONArray(customerDetailsObj);
            log.info("** Response recived: We have {} pages of records", customerObj.length());
            System.out.println(customerObj);
            for (int i = 0; i < customerObj.length(); i++) {
                JSONObject customerJSON = customerObj.getJSONObject(i);
                ObjectMapper mapper = new ObjectMapper();
                CustomerDTO customerListDTO = mapper.readValue(customerJSON.toString(), CustomerDTO.class);
                addCustomer(customerListDTO);

            }
            log.info("** record inserted {} in customer table", customerObj.length());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while adding customer details. " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> registerCustomer(CustomerDTO dto) {
        log.info("** Customer Register {} ", dto);
        try {
            validateRegisterCustomerParams(dto);
            addCustomer(dto);
            return new ResponseEntity<>("Customer registered successfully. ", HttpStatus.OK);

        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            throw new BizException("Error while registering customer. ", e.getLocalizedMessage());
        }
    }

    @Override
    public ResponseEntity<?> updateKycStatus(CustomerDTO dto) {

        log.info("inside update kyc status for mobile: {}", dto.getContactNo());
        try {
            List<Customer> custList = customerRepository.findAllByMobile(dto.getContactNo());
            if (custList == null || custList.isEmpty()) {
                log.error("No registration details found with mobile: {}", dto.getContactNo());
                throw new BizException("No registration details found with mobile " + dto.getContactNo());
            }

            if (custList != null & custList.size() != 0) {
                //check if the customer is active with mobile number
                boolean isActive = isCustomerActive(custList);
                if (!isActive) {
                    log.error("KYC status can not be upgraded! Customer is not Active");
                    throw new BizException("KYC status can not be upgraded! Customer is not Active");
                }
                for (Customer cust : custList) {
                    if (!cust.getKycType().equals(Status.FULL_KYC)) {
                        cust.setKycType(Status.FULL_KYC);
                        customerRepository.save(cust);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while upgrading KYC " + e.getMessage(), e);
            throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "error while upgrading KYC!");
        }
        ApiError response = new ApiError(HttpStatus.OK, "KYC successfully upgraded to FULL_KYC!");
        return new ResponseEntity<>("KYC successfully upgraded to FULL_KYC!", HttpStatus.OK);
    }

    private boolean validateRegisterCustomerParams(CustomerDTO dto) {

        if (StringUtils.isBlank(dto.getTitle())) {
            throw new BizException("Customer title cannot be empty. " + dto.getTitle());
        }
        if (StringUtils.isBlank(dto.getFirstName())) {
            throw new BizException("Customer first name cannot be empty. " + dto.getFirstName());
        }
        if (StringUtils.isBlank(dto.getLastName())) {
            throw new BizException("Customer last name cannot be empty. " + dto.getLastName());
        }
        if (StringUtils.isBlank(dto.getCustomerHashId())) {
            throw new BizException("Customer hash Id cannot be empty. " + dto.getCustomerHashId());
        }

        if (StringUtils.isBlank(dto.getEmailAddress())) {
            throw new BizException("Customer email address cannot be empty. " + dto.getEmailAddress());
        }
        return true;
    }

    private void addCustomer(CustomerDTO dto) {

    	try {
    		Customer customer = new Customer();
    		log.info("Storing customer with hash id {} ",dto.getCustomerHashId());
            customer.setCreatedAt(dto.getCreatedAt());
            customer.setOriginalCreatedAt(dto.getCreatedAt());
            customer.setUpdatedAt(dto.getUpdatedAt());
            customer.setCreatedBy(StringUtils.isNotEmpty(dto.getCreatedBy()) ? dto.getCreatedBy() : "");
            customer.setUpdatedBy(StringUtils.isNotEmpty(dto.getUpdatedBy()) ? dto.getUpdatedBy() : "");
            customer.setCustomerHashId(dto.getCustomerHashId());
            customer.setTitle(StringUtils.isNotEmpty(dto.getTitle()) ? dto.getTitle() : "");
            customer.setFirstName(StringUtils.isNotEmpty(dto.getFirstName()) ? dto.getFirstName() : "");
            customer.setLastName(StringUtils.isNotEmpty(dto.getLastName()) ? dto.getLastName() : "");
            customer.setCountryCode(StringUtils.isNotEmpty(dto.getCountryCode()) ? dto.getCountryCode() : "");
            customer.setMobile(StringUtils.isNotEmpty(dto.getContactNo()) ? dto.getContactNo() : "");
            customer.setGender(StringUtils.isNotEmpty(dto.getGender()) ? dto.getGender() : "");
            customer.setBirthday(StringUtils.isNotEmpty(dto.getDob()) ? dto.getDob() : "");
            customer.setIdType(StringUtils.isNotEmpty(dto.getDocumentType()) ? dto.getDocumentType() : "");
            customer.setIdNumber(StringUtils.isNotEmpty(dto.getDocumentNo()) ? dto.getDocumentNo() : "");
            customer.setIdExpiryDate(StringUtils.isNotEmpty(dto.getDocumentExpDate()) ? dto.getDocumentExpDate() : "");
            customer.setEmail(StringUtils.isNotEmpty(dto.getEmailAddress()) ? dto.getEmailAddress() : "");
            customer.setAddress1(StringUtils.isNotEmpty(dto.getAddressLine1()) ? dto.getAddressLine1() : "");
            customer.setAddress2(StringUtils.isNotEmpty(dto.getAddressLine2()) ? dto.getAddressLine2() : "");
            customer.setCity(StringUtils.isNotEmpty(dto.getCity()) ? dto.getCity() : "");
            customer.setZipCode(StringUtils.isNotEmpty(dto.getPincode()) ? dto.getPincode() : "");
            customer.setState(StringUtils.isNotEmpty(dto.getState()) ? dto.getState() : "");
            customer.setCountry(StringUtils.isNotEmpty(dto.getCountry()) ? dto.getCountry() : "");
            customer.setKycType(dto.getKycType());

            if (dto.getKycType().equals(Status.FULL_KYC)) {
                customer.setAccountTypeId(FULL_KYC);
            } else {
                customer.setAccountTypeId(MIN_KYC);
            }
            customer.setCountryOfIssue(StringUtils.isNotEmpty(dto.getCountryOfIssue()) ? dto.getCountryOfIssue() : "");
            customer.setPreferredName(StringUtils.isNotEmpty(dto.getPreferredName()) ? dto.getPreferredName() : "");
            customer.setProgramId(dto.getProgramId());

    	/*	customer.setProgramPlan(dto.getProgramType());
    		customer.setProgramName(dto.getProgramName());
    		customer.setProgramHashId(dto.getProgramHashId());*/
            customer.setStatus(dto.getStatus());

            customerRepository.save(customer);
            log.info(" Customer {} SAVED in DB.",dto.getCustomerHashId());
    	}catch(Exception e) {
    		log.error(e.getMessage(),e);
    		throw new BizException("Error while storing customer data. "+e.getMessage());
    	}
    	
        
    }

    @Override
    public ResponseEntity<?> updateCustomer(CustomerDTO dto) {
        validateRegisterCustomerParams(dto);
        editCustomer(dto);
        log.info("** Customer Updated Successfully, with customer hash id {} ", dto.getCustomerHashId());
        return new ResponseEntity<>("Customer Updated Successfully.", HttpStatus.OK);
    }

    private void editCustomer(CustomerDTO dto) {

        Customer customer = customerRepository.findByCustomerHashId(dto.getCustomerHashId());

        if (!customer.getStatus().equals(Status.ACTIVE)) {
            log.info("customer is not active");
            throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "Inactive/block customer can not be edited");
        }
        if (!customer.getMobile().equalsIgnoreCase(dto.getContactNo())) {
            log.info("error getting programPlan");
            throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "mobile number can not be updated");
        }

        customer.setCustomerHashId(dto.getCustomerHashId());
        customer.setTitle(dto.getTitle());
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setCountryCode(dto.getCountryCode());
        customer.setMobile(dto.getContactNo());
        customer.setGender(dto.getGender());
        customer.setBirthday(dto.getDob());
        customer.setIdType(dto.getDocumentType());
        customer.setIdNumber(dto.getDocumentNo());
        //customer.setIdExpiryDate(dto.getDocumentExpDate());
        customer.setEmail(dto.getEmailAddress());
        customer.setAddress1(dto.getAddressLine1());
        customer.setAddress2(dto.getAddressLine2());
        customer.setCity(dto.getCity());
        customer.setZipCode(dto.getPincode());
        customer.setState(dto.getState());
        customer.setCountry(dto.getCountry());
        //customer.setKycType(dto.getKycType());
		/*customer.setProgramPlan(dto.getProgramType());
		customer.setProgramName(dto.getProgramName());
		customer.setProgramHashId(dto.getProgramHashId());*/
        customer.setStatus(dto.getStatus());

        customerRepository.save(customer);
    }

    private void updateCustomerKyc(CustomerDTO dto) {
        List<Customer> customers = customerRepository.findAllByMobile(dto.getContactNo());
        for (Customer customer : customers) {
		/*	customer.setKycType(dto.getStatus());
			customer.setKycRequestId(dto.getKycRefNo());*/
            customerRepository.save(customer);
        }
    }

    @Override
    public Page<CustomerView> getCustomer(CustomerDTO dto) {
        try {
            log.info("** Fetching customer by filers dateRange{} | programName {} | "
                    + " customer HashID {} | customerMobile {} ", dto.getDateRange(), dto.getProgramName(), dto.getCustomerHashId(), dto.getContactNo());

            Sort sort = new Sort(Sort.Direction.DESC, "originalCreatedAt");
            Pageable pageable = PageRequest.of(Integer.parseInt(dto.getPage()), Integer.parseInt(dto.getSize()), sort);

            Date startDate = null, endDate = null;
            if (StringUtils.isNotBlank(dto.getDateRange())) {

                String dateArray[] = null;
                dateArray = dto.getDateRange().split("-");

                String startDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dateArray[0] + CommonUtil.startTime));
                startDate = new SimpleDateFormat("yyyy-MM-dd ").parse(startDate1);

                String endtDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dateArray[1] + CommonUtil.endTime));
                endDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endtDate1);
                dto.setStartDate(startDate);
                dto.setEndDate(endDate);
            }
            log.info("** Specification Created. ");
            Page<CustomerView> customers = customerViewRepository.findAll(customerSpecification(dto), pageable);
            return customers;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while fetching customer data. ", e.getMessage());
        }
    }

    private Specification<CustomerView> customerSpecification(CustomerDTO dto) {
        Specification<CustomerView> spec = (customer, cq, cb) -> cb.equal(cb.literal(1), 1);
        if (StringUtils.isNotBlank(dto.getProgramName())) {
            log.info("** Fiter ++ ProgramName {} ", dto.getProgramName());
            spec = spec.and((customer, cq, cb) -> cb.like(customer.get("programName"), dto.getProgramName() + "%"));
        }
        if (StringUtils.isNotBlank(dto.getContactNo())) {
            log.info("** Fiter ++ ContactNumber {} ", dto.getContactNo());
            spec = spec.and((customer, cq, cb) -> cb.equal(customer.get("mobile"), dto.getContactNo()));
        }
        if (StringUtils.isNotBlank(dto.getCustomerHashId())) {
            log.info("** Fiter ++ CustomerHashId {} ", dto.getCustomerHashId());
            spec = spec.and((customer, cq, cb) -> cb.equal(customer.get("customerHashId"), dto.getCustomerHashId()));
        }

        if (dto.getStartDate() != null) {
            log.info("** Fiter ++ startDate {} | endDate {} ", dto.getStartDate(), dto.getEndDate());
            spec = spec.and((customer, cq, cb) -> cb.between(customer.get("originalCreatedAt"), dto.getStartDate(), dto.getEndDate()));
        }
        return spec;
    }

    private boolean isCustomerActive(List<Customer> custList) {
        log.info("check if the customer is active in any one program");
        for (Customer cust : custList) {
            if (cust.getStatus().equals(Status.ACTIVE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResponseEntity<?> kycLimitConfig() {
        log.info("*** kyc limit config - START");
        List<String> distMobileList = customerRepository.getDistinctMobileListForGPR();
        log.info("*** distMobileList size :{}", distMobileList.size());

        if (distMobileList.isEmpty()) {
            log.info("*** No Customer List Found");
            throw new BizException("No Customer List Found", "No Customer List Found");
        }

        for (String customerMob : distMobileList) {
            log.info("*** Getting in for the transaction detail for customerMobile :{}", customerMob);
            try {
                Customer customer = customerRepository.findTopByMobileOrderByIdDesc(customerMob);
                if (customer != null) {
                    Optional<AccountType> accountTypeExist = accountTypeRepository.findById(customer.getAccountTypeId());
                    if (accountTypeExist.isPresent()) {
                        AccountType accountType = accountTypeExist.get();
                        log.info("*** AccountType Details :{}", accountType);

                        Double annualSumOfAmount = transactionService.getAnnualWalletCreditBalanceByCustomerForGPR(customerMob, TransactionTypes.Wallet_Credit_Mode_Prefund);
                        log.info("*** annualSumOfAmount :{}", annualSumOfAmount);
                        double remainingAnnualLimit = MonetaryUtil.subtract(accountType.getYearlyLimit(), annualSumOfAmount);
                        log.info("*** remainingAnnualLimit :{}", remainingAnnualLimit);

                        Double monthlySumOfAmount = transactionService.getMonthlyWalletCreditBalanceByCustomerForGPR(customerMob, TransactionTypes.Wallet_Credit_Mode_Prefund);
                        log.info("*** monthlySumOfAmount :{}", monthlySumOfAmount);
                        double remainingMonthlyLimit = MonetaryUtil.subtract(accountType.getMonthlyLimit(), monthlySumOfAmount);
                        log.info("*** remainingMonthlyLimit :{}", remainingMonthlyLimit);

                        Double currentBalance = transactionService.getCurrentBalanceByCustomerForGPR(customerMob);
                        log.info("*** currentBalance :{}", currentBalance);
                        double remainingBalanceLimit = MonetaryUtil.subtract(accountType.getBalanceLimit(), currentBalance);
                        log.info("*** remainingBalanceLimit :{}", remainingBalanceLimit);

                        /*Save kyc limits*/
                        saveKycLimit(customerMob, accountType, remainingAnnualLimit, remainingMonthlyLimit, remainingBalanceLimit);
                    }
                }
            } catch (Exception e) {
                log.error("*** Exception occurred : {}", e.getMessage());
                log.error(e.getMessage(), e);
            }
        }
        ApiError response = new ApiError(HttpStatus.OK, "Customer remaining limit saved successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public Object execute(String programUrl, CustomerKycLimitBasedOnFiltersDTO customerKycLimitBasedOnFiltersDTO) {
        log.info("*** Inside get customer kyc list data - START");
        Page<CustomerKycLimit> customerKycLimits = null;
        try {
            Sort sort = new Sort(Sort.Direction.DESC, "createdAt");
            Pageable pageable = PageRequest.of(Integer.parseInt(customerKycLimitBasedOnFiltersDTO.getPage()),
                    Integer.parseInt(customerKycLimitBasedOnFiltersDTO.getSize()), sort);

            Date startDate = null, endDate = null;
            if (!StringUtils.isEmpty(customerKycLimitBasedOnFiltersDTO.getDateRange())) {
                String dateArray[] = customerKycLimitBasedOnFiltersDTO.getDateRange().split("-");
                startDate = CommonUtil.dateFormate2.parse(dateArray[0] + CommonUtil.startTime);
                endDate = CommonUtil.dateFormate2.parse(dateArray[1] + CommonUtil.endTime);
            }

            log.info("** Filters before query are programName StartDate:{}, EndDate :{}", startDate, endDate);
            customerKycLimits = customerKycLimitRepository.findAll(customerKycLimitSpecification(customerKycLimitBasedOnFiltersDTO, startDate, endDate), pageable);
            return customerKycLimits;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(BizErrors.BAD_REQUEST_ERROR.getValue(), "Internal server error. Please try again after sometimes");
        }
    }

    @Override
    public void downloadCustomerKycLimitReport(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams) {
        log.info("*** download customer kyc limit - START");
        try {
            CustomerKycLimitBasedOnFiltersDTO dto = new CustomerKycLimitBasedOnFiltersDTO();
            dto.setDateRange(requestParams.get("dateRange"));
            dto.setMobileNumber(requestParams.get("mobileNumber"));

            Date startDate = null, endDate = null;
            if (!StringUtils.isEmpty(dto.getDateRange())) {
                String dateArray[] = dto.getDateRange().split("-");
                startDate = CommonUtil.dateFormate2.parse(dateArray[0] + CommonUtil.startTime);
                endDate = CommonUtil.dateFormate2.parse(dateArray[1] + CommonUtil.endTime);
            }

            log.info("** Filters before query are programName StartDate:{}, EndDate :{}", startDate, endDate);
            List<CustomerKycLimit> custKycLimittList = customerKycLimitRepository.findAll(customerKycLimitSpecification(dto, startDate, endDate));

            if (custKycLimittList != null)
                log.info("** Data fetched " + custKycLimittList.size());
            else
                log.info("** No DATA fetched. ");
            processCustKycLimitDownload(custKycLimittList, response);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error While processing transaction offline. ", e.getMessage());
        }
    }

    private void processCustKycLimitDownload(List<CustomerKycLimit> customerKycLimits, HttpServletResponse response2) {
        if (customerKycLimits instanceof List<?>) {
            String resultantFileName = "recon-report.xlsx";
            log.info("--> Filename : " + resultantFileName + " Initiated..");
            final List<CustomerKycLimit> transactionList = (List<CustomerKycLimit>) customerKycLimits;
            if (transactionList.size() == 0) {
                throw new BizException("No recon reports!");
            }
            final List<Map<String, String>> resultantData = new ArrayList<Map<String, String>>();
            final List<String> headers = new ArrayList<String>();

            headers.add("TRANSACTION DATE");
            headers.add("MOBILE NUMBER");
            headers.add("KYC TYPE");
            headers.add("BALANCE LIMIT");
            headers.add("MONTHLY LIMIT");
            headers.add("YEAR LIMIT");
            headers.add("REMAINING BALANCE LIMIT");
            headers.add("REMAINING MONTHLY LIMIT");
            headers.add("REMAINING YEARLY LIMIT");

            for (CustomerKycLimit processReportView : customerKycLimits) {
                final Map<String, String> dataMap = new HashMap<String, String>();

                final String transactionDate = processReportView.getTransactionDate() + "";
                final String mobileNumber = StringUtils.isNotEmpty(processReportView.getMobileNumber()) ? processReportView.getMobileNumber() : "NA";
                final String kycType = processReportView.getKycType().getValue();
                final String balanceLimit = processReportView.getBalanceLimit() + "";
                final String monthlyLimit = processReportView.getMonthlyLimit() + "";
                final String yearLimit = processReportView.getYearlyLimit() + "";
                final String remainingBalanceLimit = processReportView.getRemainingBalanceLimit() + "";
                final String remainingMonthlyLimit = processReportView.getRemainingMonthlyLimit() + "";
                final String remainingYearlyLimit = processReportView.getRemainingYearlyLimit() + "";

                dataMap.put("TRANSACTION DATE", transactionDate);
                dataMap.put("MOBILE NUMBER", mobileNumber);
                dataMap.put("KYC TYPE", kycType);
                dataMap.put("BALANCE LIMIT", balanceLimit);
                dataMap.put("MONTHLY LIMIT", monthlyLimit);
                dataMap.put("YEAR LIMIT", yearLimit);
                dataMap.put("REMAINING BALANCE LIMIT", remainingBalanceLimit);
                dataMap.put("REMAINING MONTHLY LIMIT", remainingMonthlyLimit);
                dataMap.put("REMAINING YEARLY LIMIT", remainingYearlyLimit);
                resultantData.add(dataMap);
            }
            log.info("--> Headers for excel : " + headers + " |   DataMap : " + resultantData);
            CommonUtil.generateExcelSheet(headers, resultantData, resultantFileName, response2);
        }
    }

    private Specification<CustomerKycLimit> customerKycLimitSpecification(CustomerKycLimitBasedOnFiltersDTO customerKycLimitBasedOnFiltersDTO,
                                                                          Date startDate, Date endDate) {
        Specification<CustomerKycLimit> spec = null;

        try {
            spec = (CustomerKycLimit, mq, mb) -> mb.equal(mb.literal(1), 1);

            if (startDate != null) {
                spec = spec.and((customerKycLimitRoot, cq, cb) -> cb.between(customerKycLimitRoot.get("transactionDate"), startDate, endDate));
            }

            if (!StringUtils.isBlank(customerKycLimitBasedOnFiltersDTO.getMobileNumber()) && customerKycLimitBasedOnFiltersDTO.getMobileNumber() != "") {
                spec = spec.and((CustomerKycLimit, mq, mb) -> mb.equal(CustomerKycLimit.get("mobileNumber"), customerKycLimitBasedOnFiltersDTO.getMobileNumber()));
            }

        } catch (Exception e) {
            log.error("*** ");
            throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "Internal server error. Please try again after sometimes");
        }
        return spec;
    }


    private void saveKycLimit(String customerMob, AccountType accountType, double remainingAnnualLimit, double remainingMonthlyLimit, double remainingBalanceLimit) {
        log.info("*** inside save KYC Limit for customerMob :{}", customerMob);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
        log.info("DATE :{}", cal.getTime());
        CustomerKycLimit customerKycLimit = new CustomerKycLimit();
        customerKycLimit.setCreatedAt(new Date());
        customerKycLimit.setUpdatedAt(new Date());
        customerKycLimit.setTransactionDate(cal.getTime());
        customerKycLimit.setMobileNumber(customerMob);
        customerKycLimit.setKycType(accountType.getCode());
        customerKycLimit.setBalanceLimit(accountType.getBalanceLimit());
        customerKycLimit.setMonthlyLimit(accountType.getMonthlyLimit());
        customerKycLimit.setYearlyLimit(accountType.getYearlyLimit());
        customerKycLimit.setRemainingBalanceLimit(remainingBalanceLimit);
        customerKycLimit.setRemainingMonthlyLimit(remainingMonthlyLimit);
        customerKycLimit.setRemainingYearlyLimit(remainingAnnualLimit);
        customerKycLimitRepository.save(customerKycLimit);
        log.info("*** saved customer kyc limit successfully");
    }

    private void saveAndUpdateKycLimit(String customerMob, AccountType accountType, double remainingAnnualLimit, double remainingMonthlyLimit, double remainingBalanceLimit) {
        log.info("*** inside save KYC Limit for customerMob :{}", customerMob);
        CustomerKycLimit customerKycLimit = customerKycLimitRepository.findByMobileNumber(customerMob);
        if (customerKycLimit == null) {
            customerKycLimit = new CustomerKycLimit();
            customerKycLimit.setCreatedAt(new Date());
            customerKycLimit.setUpdatedAt(new Date());
            customerKycLimit.setMobileNumber(customerMob);
            customerKycLimit.setKycType(accountType.getCode());
            customerKycLimit.setBalanceLimit(accountType.getBalanceLimit());
            customerKycLimit.setMonthlyLimit(accountType.getMonthlyLimit());
            customerKycLimit.setYearlyLimit(accountType.getYearlyLimit());
            customerKycLimit.setRemainingBalanceLimit(remainingBalanceLimit);
            customerKycLimit.setRemainingMonthlyLimit(remainingMonthlyLimit);
            customerKycLimit.setRemainingYearlyLimit(remainingAnnualLimit);
            customerKycLimitRepository.save(customerKycLimit);
            log.info("*** saved customer kyc limit successfully");
        } else {
            customerKycLimit.setRemainingBalanceLimit(remainingBalanceLimit);
            customerKycLimit.setRemainingMonthlyLimit(remainingMonthlyLimit);
            customerKycLimit.setRemainingYearlyLimit(remainingAnnualLimit);
            customerKycLimitRepository.save(customerKycLimit);
            log.info("*** customer kyc limit successfully updated");
        }
    }

    @Override
    public void kycLimitConfigByCron() {
        log.info("*** kyc limit config By Cron - START");
        try {
            List<String> distMobileList = customerRepository.getDistinctMobileListForGPR();
            log.info("*** distMobileList size :{}", distMobileList.size());

            if (!distMobileList.isEmpty()) {

                int txnCount = 0, txnCountTillNow = 0;
                for (String customerMob : distMobileList) {
                    log.info("*** Getting in for the transaction detail for customerMobile :{}", customerMob);
                    try {
                        Customer customer = customerRepository.findTopByMobileOrderByIdDesc(customerMob);
                        if (customer != null) {
                            Optional<AccountType> accountTypeExist = accountTypeRepository.findById(customer.getAccountTypeId());
                            if (accountTypeExist.isPresent()) {
                                AccountType accountType = accountTypeExist.get();
                                log.info("*** AccountType Details :{}", accountType);

                                Double annualSumOfAmount = transactionService.getAnnualWalletCreditBalanceByCustomerForGPR(customerMob, TransactionTypes.Wallet_Credit_Mode_Prefund);
                                log.info("*** annualSumOfAmount :{}", annualSumOfAmount);
                                double remainingAnnualLimit = MonetaryUtil.subtract(accountType.getYearlyLimit(), annualSumOfAmount);
                                log.info("*** remainingAnnualLimit :{}", remainingAnnualLimit);

                                Double monthlySumOfAmount = transactionService.getMonthlyWalletCreditBalanceByCustomerForGPR(customerMob, TransactionTypes.Wallet_Credit_Mode_Prefund);
                                log.info("*** monthlySumOfAmount :{}", monthlySumOfAmount);
                                double remainingMonthlyLimit = MonetaryUtil.subtract(accountType.getMonthlyLimit(), monthlySumOfAmount);
                                log.info("*** remainingMonthlyLimit :{}", remainingMonthlyLimit);

                                Double currentBalance = transactionService.getCurrentBalanceByCustomerForGPR(customerMob);
                                log.info("*** currentBalance :{}", currentBalance);
                                double remainingBalanceLimit = MonetaryUtil.subtract(accountType.getBalanceLimit(), currentBalance);
                                log.info("*** remainingBalanceLimit :{}", remainingBalanceLimit);

                                /*Save kyc limits*/
                                saveKycLimit(customerMob, accountType, remainingAnnualLimit, remainingMonthlyLimit, remainingBalanceLimit);

                                txnCount++;
                                log.info("txnCount : {}", txnCount);
                                txnCountTillNow++;
                                log.info("Txn Count till now :{}", txnCountTillNow);
                                if (txnCount > 500) {
                                    log.info("Txn count is more then :{}, let's sleep for 5sec", txnCount);
                                    txnCount = 0;
                                    Thread.sleep(5000);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("*** Exception occurred : {}", e.getMessage());
                        log.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("*** Exception occurred : {}", e.getMessage());
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> updateKycLimit(CustomerDTO dto) {
        log.info("* * Request body Balance Limit:{} ,Monthly Load Limit:{} , Annual Load Limit:{}, Account type:{}",
                dto.getBalanceLimit(), dto.getMonthlyLoadLimit(), dto.getAnnualLoadLimit(),
                dto.getAccountType());
        try {
            AccountType accountType = accountTypeRepository.findByCode(AccountCode.valueOf(dto.getAccountType()));

            if (accountType == null) {
                log.error("No record fund In Sor Account Type for Code: {}", dto.getAccountType());
                throw new BizException("No record fund In Sor Account Type for Code: {}", dto.getAccountType());
            }
            accountType.setBalanceLimit(Double.parseDouble(dto.getBalanceLimit()));
            accountType.setMonthlyLimit(Double.parseDouble(dto.getMonthlyLoadLimit()));
            accountType.setYearlyLimit(Double.parseDouble(dto.getAnnualLoadLimit()));
            accountTypeRepository.save(accountType);
            return new ResponseEntity<>("KYC LIMIT successfully upgrading!", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while upgrading KYC Limit in Sor " + e.getMessage(), e);
            throw new BizException("error while upgrading KYC limit! "+e.getMessage());
        }

    }
}