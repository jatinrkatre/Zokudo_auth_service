package com.zokudo.sor.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.zokudo.sor.enums.TransactionTypes;
import com.zokudo.sor.util.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zokudo.sor.dto.TransactionDTO;
import com.zokudo.sor.entities.CronFailure;
import com.zokudo.sor.entities.CronReport;
import com.zokudo.sor.entities.CustomerEODBalance;
import com.zokudo.sor.entities.CustomerEODBalanceSumView;
import com.zokudo.sor.entities.CustomerEODBalanceView;
import com.zokudo.sor.entities.CustomerEODSumBalance;
import com.zokudo.sor.entities.EODBalanceData;
import com.zokudo.sor.entities.Transaction;
import com.zokudo.sor.enums.BizErrors;
import com.zokudo.sor.enums.CronName;
import com.zokudo.sor.enums.ProgramPlans;
import com.zokudo.sor.enums.Quater;
import com.zokudo.sor.enums.Status;
import com.zokudo.sor.exceptions.BizException;
import com.zokudo.sor.repositories.CronFailureRepository;
import com.zokudo.sor.repositories.CronReportRepository;
import com.zokudo.sor.repositories.CustomerEODBalanceRepository;
import com.zokudo.sor.repositories.CustomerEODBalanceSumRepository;
import com.zokudo.sor.repositories.CustomerEODBalanceSumViewRepository;
import com.zokudo.sor.repositories.CustomerEODBalanceViewRepository;
import com.zokudo.sor.repositories.EODBalanceRepository;
import com.zokudo.sor.repositories.TransactionRepository;
import com.zokudo.sor.service.TransactionService;
import com.zokudo.sor.util.CommonUtil;
import com.zokudo.sor.util.UrlMetaData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private static long recordCount;
    private final Client client;
    private final UrlMetaData urlMetaData;
    private final String applicationLevelUserName;
    private final String applicationLevelUserPassword;
    private final TransactionRepository transactionRepository;
    private final EODBalanceRepository eodBalanceRepository;
    private final CustomerEODBalanceRepository customerEODBalanceRepository;
    private final CustomerEODBalanceViewRepository customerEODBalanceViewRepository;
    private final CustomerEODBalanceSumRepository customerEODBalanceSumRepository;
    private final CustomerEODBalanceSumViewRepository customerEODBalanceSumViewRepository;
    private final CronReportRepository cronReportRepository;
    private final CronFailureRepository cronFailureRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public TransactionServiceImpl(final Client client, final UrlMetaData urlMetaData,
                                  @Value("${applicationLevel.user.name}") String applicationLevelUserName,
                                  @Value("${applicationLevel.user.password}") String applicationLevelUserPassword,
                                  final TransactionRepository transactionRepository,
                                  final EODBalanceRepository eodBalanceRepository,
                                  final CustomerEODBalanceRepository customerEODBalanceRepository,
                                  final CustomerEODBalanceViewRepository customerEODBalanceViewRepository,
                                  final CustomerEODBalanceSumRepository customerEODBalanceSumRepository,
                                  final CustomerEODBalanceSumViewRepository customerEODBalanceSumViewRepository,
                                  final CronReportRepository cronReportRepository,
                                  final CronFailureRepository cronFailureRepository, SecurityUtil securityUtil) {
        this.client = client;
        this.urlMetaData = urlMetaData;
        this.applicationLevelUserName = applicationLevelUserName;
        this.applicationLevelUserPassword = applicationLevelUserPassword;
        this.transactionRepository = transactionRepository;
        this.eodBalanceRepository = eodBalanceRepository;
        this.customerEODBalanceRepository = customerEODBalanceRepository;
        this.customerEODBalanceViewRepository = customerEODBalanceViewRepository;
        this.customerEODBalanceSumRepository = customerEODBalanceSumRepository;
        this.customerEODBalanceSumViewRepository = customerEODBalanceSumViewRepository;
        this.cronReportRepository = cronReportRepository;
        this.cronFailureRepository = cronFailureRepository;
        this.securityUtil = securityUtil;
    }


    @Override
    public Page<Transaction> getTransaction(TransactionDTO dto) {
        log.info("** Fetch Transaction Details by filters : Date Filter {} |  Mobile {} | proxy card no {} | programname {} ", dto.getDateRange(), dto.getMobileNumber(),
                dto.getProxyCardNo(), dto.getProgramName());
        try {
            Sort sort = new Sort(Sort.Direction.DESC, "transactionCreatedAt");
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
            Page<Transaction> transactions = transactionRepository.findAll(transactionSpecification(dto), pageable);
            return transactions;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while fetching transaction list. ", e.getMessage());
        }
    }

    @Override
    public void storeCustomerTransaction() {
    	
    	long rCount = 0l;
    	long begin = System.currentTimeMillis();
    	CronReport report = new CronReport();
    	report.setCreatedAt(new Date());
		report.setUpdatedAt(new Date());
		report.setCreatedBy("admin");
		report.setCronName(CronName.CUSTOMER_TRANSACTION);
		TransactionDTO dto = new TransactionDTO();
        try {
            recordCount = 0l;
            if(hasFailedRecord(CronName.CUSTOMER_TRANSACTION,dto)) {
            	log.info("** Processing cron for failed scenario. ");
            	processForFailTxn(dto);
            	report.setStatus(Status.SUCCESS);
               
            }else {
            	
            	 Date previousDate = CommonUtil.getPastDateFromCurrentDate();
            	 dto.setPreviousDate(previousDate);
            	 
                 String startDate = CommonUtil.getQuarterStartTime(Quater.FIRST_QUARTER, previousDate);
                 String endDate = CommonUtil.getQuarterEndTime(Quater.FIRST_QUARTER, previousDate);

                 log.info("** Fetching transaction for Quarter 1, start {} end {} ", startDate, endDate);
                 rCount = CustomerTransactionByDate(startDate, endDate,rCount);
                 //Thread.sleep(5000);

                 startDate = CommonUtil.getQuarterStartTime(Quater.SECOND_QUARTER, previousDate);
                 endDate = CommonUtil.getQuarterEndTime(Quater.SECOND_QUARTER, previousDate);
                 log.info("** Fetching transaction for Quarter 2, start  {} end  {} ", startDate, endDate);
                 rCount = CustomerTransactionByDate(startDate, endDate,rCount);
                 //Thread.sleep(5000);

                 startDate = CommonUtil.getQuarterStartTime(Quater.THIRD_QUARTER, previousDate);
                 endDate = CommonUtil.getQuarterEndTime(Quater.THIRD_QUARTER, previousDate);
                 log.info("** Fetching transaction for Quarter 3, start {} end {} ", startDate, endDate);
                 rCount = CustomerTransactionByDate(startDate, endDate,rCount);
                 Thread.sleep(3000);

                 startDate = CommonUtil.getQuarterStartTime(Quater.FOURTH_QUARTER, previousDate);
                 endDate = CommonUtil.getQuarterEndTime(Quater.FOURTH_QUARTER, previousDate);
                 log.info("** Fetching transaction for Quarter 4, start {} end {} ", startDate, endDate);
                 rCount = CustomerTransactionByDate(startDate, endDate,rCount);
                 //Thread.sleep(2000);

                 long endTime = System.currentTimeMillis();
                 double time = (endTime - begin) / 1000;
                 report.setStatus(Status.SUCCESS);
                 log.info("** Time Required to fetch {} records is: {}s ", recordCount, time);
            }

        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        	String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
        	report.setComment(err);
			report.setStatus(Status.FAILURE);
			Date failureDate = dto.getStartDate() != null ? dto.getStartDate() : dto.getPreviousDate();
			reportCronFailure(failureDate,CronName.CUSTOMER_TRANSACTION,err);
			//TODO : 1. Report the error with cron type. 2. Store the date(which will become start date in failure) 
            throw new BizException("Error while storing cutomer txn. ", e.getMessage());
        }finally {
        	
        	long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(rCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
        }
    }

    private long CustomerTransactionByDate(String startDate, String endDate,long rCount) {

        final JSONObject requestParameters = new JSONObject();
        try {


            requestParameters.put("start", startDate);
            requestParameters.put("end", endDate);
            final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
            headerMap.add("Authorization", securityUtil.getAuthorizationHeader());
            headerMap.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            String strURl = urlMetaData.GET_TRANSACTION_DETAILS_INCREMENTAL;

            Response clientResponse = client.target(strURl)
                    .request()
                    .headers(headerMap)
                    .post(Entity.entity(requestParameters.toString(), MediaType.APPLICATION_JSON_VALUE));

            String transactionDetails = clientResponse.readEntity(String.class);
            if (clientResponse.getStatus() != 200)
                throw new BizException(BizErrors.DATA_NOT_FOUND.getValue(), "Unable to fetch Transaction list!");

            JSONArray transactionObj = new JSONArray(transactionDetails);
            log.info("** Response recieved: WE have {} pages of records. ", transactionObj.length());
            System.out.println(transactionObj);
            for (int i = 0; i < transactionObj.length(); i++) {
                JSONObject transactionContent = transactionObj.getJSONObject(i);
                JSONArray transactions = transactionContent.getJSONArray("content");

                ObjectMapper mapper = new ObjectMapper();
                List<TransactionDTO> transactionListDTO = mapper.readValue(transactions.toString(), new TypeReference<List<TransactionDTO>>() {
                });
                rCount = processEachTransaction(transactionListDTO,rCount);
            }

            return rCount;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return rCount;
        }

    }

    private long processEachTransaction(List<TransactionDTO> transactionListDTO,long rCount) throws InterruptedException {
        //log.info("** Transaction List : {}",transactionListDTO.size());
        long transactionCount = 0l;
        for (TransactionDTO dto : transactionListDTO) {


            Transaction transaction = new Transaction();
            log.info("** Befor Saving Txn request into db for transactionRefNo {} and createdAt {} : " ,dto.getTransactionRefNo(),dto.getCreatedAt());

            transaction.setTransactionId(dto.getId());
            transaction.setClientName(dto.getClientName());
            transaction.setAgentHashId(dto.getAgentHashId());
            transaction.setBillingAmt(dto.getBillingAmt());
            transaction.setCardType(dto.getCardType());
            transaction.setCashBackAmt(dto.getCashBackAmt());
            transaction.setEntityName(dto.getEntityName());
            transaction.setComments(dto.getComments());
            transaction.setCurrentBalance(dto.getCurrentBalance());
            transaction.setCustomerMobile(dto.getCustomerMobile());
            transaction.setCustomerName(dto.getCustomerName());
            transaction.setDistributorHashId(dto.getDistributorHashId());
            transaction.setDistributorCompany(dto.getDistributorCompany());
            transaction.setEmail(dto.getEmail());
            transaction.setMaskCardNumber(dto.getMaskCardNumber());
            transaction.setMerchantName(dto.getMerchantName());
            transaction.setMerchantId(dto.getMerchantId());
            transaction.setOriginalAmount(dto.getOriginalAmount());
            transaction.setOriginalTxnDate(dto.getOriginalTxnDate());
            transaction.setPocketName(dto.getPocketName());
            transaction.setProgramName(dto.getProgramName());
            transaction.setProxyCardNo(dto.getProxyCardNo());
            transaction.setRetrivalRefNo(dto.getRetrivalRefNo());
            transaction.setStatus(dto.getStatus());
            transaction.setTransactionAmt(dto.getTransactionAmt());
            transaction.setTransactionRefNo(dto.getTransactionRefNo());
            transaction.setTransactionType(dto.getTransactionType());
            transaction.setWalletHashId(dto.getWalletHashId());
            transaction.setUpdatedAt(dto.getUpdatedAt());
            transaction.setAgentName(dto.getAgentName());
            transaction.setDistributorName(dto.getDistributorName());
            transaction.setCreatedAt(dto.getCreatedAt());
            transaction.setTransactionCreatedAt(dto.getCreatedAt());
            transaction.setCustomerHashId(dto.getCustomerHashId());
            transactionRepository.save(transaction);
            log.info("** After Saving Txn request into db for transactionRefNo {} and createdAt {} : " ,transaction.getTransactionRefNo(),transaction.getCreatedAt());
            transactionCount++;
            if (transactionCount % 20 == 0) {
                log.info("** DB sleep after batch of record count {} ", transactionCount);
                Thread.sleep(5000);
            }
            recordCount++;
            rCount++;

        }
        return rCount;
    }

    private Specification<Transaction> transactionSpecification(TransactionDTO dto) {
        Specification<Transaction> spec = (root, mq, mb) -> mb.equal(mb.literal(1), 1);
        try {
            if (StringUtils.isNotBlank(dto.getMobileNumber())) {
                log.info("** Filter ++ customer mobile {} ", dto.getMobileNumber());
                spec = spec.and((root, mq, mb) -> mb.equal(root.get("customerMobile"), dto.getMobileNumber()));
            }

            if (StringUtils.isNotBlank(dto.getProxyCardNo())) {
                log.info("** Filter ++ proxy Card No {} ", dto.getProxyCardNo());
                spec = spec.and((root, mq, mb) -> mb.equal(root.get("proxyCardNo"), dto.getProxyCardNo()));
            }
            if (StringUtils.isNotBlank(dto.getProgramName())) {
                log.info("** Filter ++ program name {} ", dto.getProgramName());
                spec = spec.and((root, mq, mb) -> mb.like(root.get("programName"), dto.getProgramName()));
            }

            if (dto.getStartDate() != null && dto.getEndDate() != null) {
                log.info("** Filter added startdate : {} | endDate :  {} ", dto.getStartDate(), dto.getEndDate());
                spec = spec.and((root, mq, mb) -> mb.between(root.get("transactionCreatedAt"), dto.getStartDate(), dto.getEndDate()));
            }

            if (dto.getTranasctionTypes() != null && dto.getTranasctionTypes().size() > 0) {
                List<String> txnTypes = new ArrayList<>();
                for (TransactionTypes transactionType : dto.getTranasctionTypes()) {
                    txnTypes.add(transactionType.getValue());
                }
                spec = spec.and((root, mq, mb) -> root.get("transactionType").in(txnTypes));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BizException(BizErrors.BAD_REQUEST_ERROR.getValue(), "Internal server error. Please try again after sometimes");
        }

        return spec;
    }


    @Override
    public void setUpEODBalanceIncremental() {

        log.info("Inside Setup EOD Balance Data for Current Day");
        TransactionDTO dto = new TransactionDTO();
        CronReport report = new CronReport();
        long rCount = 0l;
    	long begin = System.currentTimeMillis();
        try {
        	
    		report.setCreatedAt(new Date());
    		report.setUpdatedAt(new Date());
    		report.setCreatedBy("admin");
    		report.setCronName(CronName.SET_EOD_INCREMENTAL);
        	
    		if(hasFailedRecord(CronName.CUSTOMER_TRANSACTION,dto)) {
            	log.info("** Processing cron for failed scenario. ");
            	processForFailCaseEODBalance(dto);
            	report.setStatus(Status.SUCCESS);
               
            }else {
            	
            	 Date pastDayDate = CommonUtil.getPastDateFromCurrentDate();

                 List<String> cardWithPocketList = transactionRepository.getGroupProxyCardByDate(pastDayDate);
                 if (cardWithPocketList.isEmpty()) {
                     log.error("No data found");
                     throw new BizException("No data found");
                 }

                 int txnCount = 0, txnCountTillNow = 0;
                 for (String proxyCardNo : cardWithPocketList) {
                     try {
                         log.info("finding txn list for proxyCardNo :{}", proxyCardNo);
                         if (proxyCardNo != null) {
                             List<Transaction> txnViewList = transactionRepository.getAllByProxyCard(proxyCardNo, pastDayDate);
                             log.info("TxnViewList Size :{}", txnViewList.size());

                             if (!txnViewList.isEmpty()) {
                                 List<Transaction> pocketBasedTxnList = txnViewList.stream()
                                         .filter(distinctByKey(p -> p.getPocketName()))
                                         .collect(Collectors.toList());
                                 // CUST A--> POCKET FOOD-10k,DEFAULT-10k ,CUST B, CUST C ;
                                 double sumOfAllPocketBalance = pocketBasedTxnList.stream().mapToDouble(s -> s.getCurrentBalance()).sum();
                                 log.info("Sum of all pocketBalances :{}", sumOfAllPocketBalance);
                                 Transaction transactionView = pocketBasedTxnList.get(0);

                                 /*Save/Update the EOD Data*/
                                 saveOrUpdateEodData(sumOfAllPocketBalance, transactionView);

                                 txnCount++;
                                 log.info("txnCount : {}", txnCount);
                                 txnCountTillNow++;
                                 log.info("Txn Count till now :{}", txnCountTillNow);
                                 if (txnCount > 100) {
                                     log.info("Txn count is more then :{}, let's sleep for 5sec", txnCount);
                                     txnCount = 0;
                                     Thread.sleep(5000);
                                 }
                             }
                         }
                     } catch (Exception e) {
                         log.error("Exception occurred while set or update the EOD Data on Daily Basis");
                         log.error(e.getMessage(), e);
                     }
                 }

                 log.info("Setup EOD Balance Data for Prev Day completed!");
                 //updateCustomerBalance();
            	
            }
           
        } catch (Exception e) {
            log.error("Exception occurred while set or update the EOD Data or setting customer balance on Daily Basis");
            log.error(e.getMessage(), e);
            String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
        	report.setComment(err);
			report.setStatus(Status.FAILURE);
			Date failureDate = dto.getStartDate() != null ? dto.getStartDate() : dto.getPreviousDate();
			reportCronFailure(failureDate,CronName.SET_EOD_INCREMENTAL,err);
            
        }finally {
        	long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(rCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
        }
    }



	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private void saveOrUpdateEodData(double sumOfAllPocketBalance, Transaction transaction) {
        log.info("Save EOD Data for ProxyCardNO : {} and createdAt : {}", transaction.getProxyCardNo(),transaction.getTransactionCreatedAt());
        try {
            EODBalanceData eodBalanceData = new EODBalanceData();

            eodBalanceData.setTransactionDate(transaction.getTransactionCreatedAt());
            eodBalanceData.setCreatedAt(new Date());
            eodBalanceData.setUpdatedAt(new Date());
            eodBalanceData.setProxyCardNo(transaction.getProxyCardNo());
            eodBalanceData.setCardType(transaction.getCardType());
            eodBalanceData.setClientName(transaction.getClientName());
            eodBalanceData.setProgramName(transaction.getProgramName());
            eodBalanceData.setCurrentBalance(sumOfAllPocketBalance);
            eodBalanceData.setCustomerHashId(transaction.getCustomerHashId());
            eodBalanceData.setCustomerName(transaction.getCustomerName());
            eodBalanceData.setCustomerMobile(transaction.getCustomerMobile());
            eodBalanceData.setExpired(isGCExpired(transaction.getCardType(), sumOfAllPocketBalance, transaction.getProxyCardNo()));
            eodBalanceRepository.save(eodBalanceData);
            log.info("EOD Data Setup done for proxyCardNo : {}, and createdAt : {} ", transaction.getProxyCardNo(),transaction.getTransactionCreatedAt());
        } catch (Exception e) {
            log.error("Exception occurred while save EOD Data for ProxyCardNo :{}", transaction.getProxyCardNo());
            log.error(e.getMessage(), e);
        }
    }

    private String isGCExpired(String cardType, double currentBalance, String proxyCardNo) {
        if (StringUtils.isNotEmpty(cardType)) {
            if (cardType.toUpperCase().contains("GC") && currentBalance <= 0) {
                log.info("** Expired proxy GC Card  : " + proxyCardNo);
                return "Expired";
            }
        }
        return "-";
    }

    /**
     * This balance is calculated program level.
     */
    @Override
    public void updateCustomerBalance() {
        log.info("** Update Customer Balance Date Wise. ");
        long begin = System.currentTimeMillis();
        int count = 0;
        long totalCount = 0l;
        TransactionDTO dto = new TransactionDTO();
        CronReport report = new CronReport();
        try {
        	
        	report.setCreatedAt(new Date());
    		report.setUpdatedAt(new Date());
    		report.setCreatedBy("admin");
    		report.setCronName(CronName.SET_CUSTOMER_BALANCE_BY_PROGRAM);
    		
    		if(hasFailedRecord(CronName.SET_CUSTOMER_BALANCE_BY_PROGRAM,dto)) {
            	log.info("** Processing cron for failed scenario. ");
            	processForFailCaseCustomerProgramBalance(dto);
            	report.setStatus(Status.SUCCESS);
               
            }else {
            	Date previousDate = CommonUtil.getPastDateFromCurrentDate();
                log.info("** Previous Date {}", previousDate);
                List<String> customerHashIds = eodBalanceRepository.getGroupByCustomerHash();
                log.info("** Total customers are {} ", customerHashIds.size());
                for (String customerHashId : customerHashIds) {
                    if (customerHashId != null) {
                        Double balance = eodBalanceRepository.findSumOfBalanceGroupByCustomer(customerHashId);
                        CustomerEODBalance customerEODBalance = new CustomerEODBalance();
                        customerEODBalance.setCreatedAt(new Date());
                        customerEODBalance.setUpdatedAt(new Date());
                        customerEODBalance.setEodCreatedAt(previousDate);
                        customerEODBalance.setCustomerHashId(customerHashId);
                        customerEODBalance.setBalance(balance);
                        customerEODBalanceRepository.save(customerEODBalance);
                        count++;
                        totalCount++;
                        if (count == 50) {
                            log.info("** Thread sleep for 3s");
                            Thread.sleep(3000);
                            count = 0;
                        }
                    }
                }
                long end = System.currentTimeMillis();
                long timeTaken = (end - begin) / 1000;
                log.info("** Total customer-counter :{} , timeTaken: {}s ", totalCount, timeTaken);
                //updateCustomerBalanceSum();
            }
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
        	report.setComment(err);
			report.setStatus(Status.FAILURE);
			Date failureDate = dto.getStartDate() != null ? dto.getStartDate() : dto.getPreviousDate();
			reportCronFailure(failureDate,CronName.SET_CUSTOMER_BALANCE_BY_PROGRAM,err);
            throw new BizException("Error while updating customer balance.", e.getMessage());
        }finally {
        	long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(totalCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
        }

    }


	@Override
    public Page<CustomerEODBalanceView> getCustomerEODBalance(TransactionDTO dto) {


        log.info("** Fetch Customer Balance Details by filters : Date Filter {} |  Mobile {} | customer Name {} ", dto.getDateRange(), dto.getMobileNumber(),
                dto.getCustomerName());
        try {
            Sort sort = new Sort(Sort.Direction.DESC, "lastUpdatedAt");
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
            Page<CustomerEODBalanceView> customerBalances = customerEODBalanceViewRepository.findAll(customerEODBalanceViewSpecification(dto), pageable);
            return customerBalances;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while fetching transaction list. ", e.getMessage());
        }
    }

    private Specification<CustomerEODBalanceView> customerEODBalanceViewSpecification(TransactionDTO dto) {
        Specification<CustomerEODBalanceView> spec = (root, mq, mb) -> mb.equal(mb.literal(1), 1);

        if (StringUtils.isNotBlank(dto.getMobileNumber())) {
            log.info("** Filter ++ customer mobile {} ", dto.getMobileNumber());
            spec = spec.and((root, mq, mb) -> mb.equal(root.get("mobile"), dto.getMobileNumber()));
        }

        if (StringUtils.isNotBlank(dto.getCustomerName())) {
            log.info("** Filter ++ cutomer name {} ", dto.getCustomerName());
            spec = spec.and((root, mq, mb) -> mb.like(root.get("cutomerName"), dto.getCustomerName() + "%"));
        }
        if (StringUtils.isNotBlank(dto.getCustomerHashId())) {
            log.info("** Filter ++ cutomer hashID {} ", dto.getCustomerHashId());
            spec = spec.and((root, mq, mb) -> mb.equal(root.get("customerHashId"), dto.getCustomerHashId() + "%"));
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            log.info("** Filter added startdate : {} | endDate :  {} ", dto.getStartDate(), dto.getEndDate());
            spec = spec.and((root, mq, mb) -> mb.between(root.get("lastUpdatedAt"), dto.getStartDate(), dto.getEndDate()));
        }
        return spec;
    }


    // Get customer sum balance of by mobile.
    @Override
    public void updateCustomerBalanceSum() {

        log.info("** Update Customer Total/Sum Balance Date Wise. ");
        long begin = System.currentTimeMillis();
        int count = 0;
        long totalCount = 0l;
        double GPR_Balance;
        double GC_Balance;
        TransactionDTO dto = new TransactionDTO();
        CronReport report = new CronReport();
        try {
        	
        	report.setCreatedAt(new Date());
    		report.setUpdatedAt(new Date());
    		report.setCreatedBy("admin");
    		report.setCronName(CronName.SET_CUSTOMER_BALANCE_SUM_BY_PROGRAM);
    		
    		if(hasFailedRecord(CronName.SET_CUSTOMER_BALANCE_SUM_BY_PROGRAM,dto)) {
            	log.info("** Processing cron for failed scenario. ");
            	processForFailCaseCustomerProgramBalance(dto);
            	report.setStatus(Status.SUCCESS);
               
            }else {
            	  Date previousDate = CommonUtil.getPastDateFromCurrentDate();
                  log.info("** Previous Date {}", previousDate);
                  List<String> customerMobile = eodBalanceRepository.getGroupByCustomerMobile();
                  log.info("** Total customers mobiles are {} ", customerMobile.size());
                  for (String mobile : customerMobile) {
                      if (mobile != null) {
                          //Double balance = eodBalanceRepository.findSumOfBalanceGroupByMobile(mobile);
                          GPR_Balance = eodBalanceRepository.findSumOfGPRBalanceGroupByMobile(mobile) == null ? 0 : eodBalanceRepository.findSumOfGPRBalanceGroupByMobile(mobile);
                          GC_Balance = eodBalanceRepository.findSumOfGCBalanceGroupByMobile(mobile) == null ? 0 : eodBalanceRepository.findSumOfGCBalanceGroupByMobile(mobile);
                          CustomerEODSumBalance customerEODSumBalance = new CustomerEODSumBalance();
                          customerEODSumBalance.setCreatedAt(new Date());
                          customerEODSumBalance.setUpdatedAt(new Date());
                          customerEODSumBalance.setEodCreatedAt(previousDate);
                          customerEODSumBalance.setCustomerMobile(mobile);
                          ;
                          customerEODSumBalance.setGprBalance(GPR_Balance);
                          customerEODSumBalance.setGcBalance(GC_Balance);
                          customerEODBalanceSumRepository.save(customerEODSumBalance);
                          count++;
                          totalCount++;
                          if (count == 50) {
                              log.info("** Thread sleep for 3s");
                              Thread.sleep(3000);
                              count = 0;
                          }
                      }
                  }
                  long end = System.currentTimeMillis();
                  long timeTaken = (end - begin) / 1000;
                  log.info("** Total customer-counter :{} , timeTaken: {}s ", totalCount, timeTaken);
            	
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        	String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
        	report.setComment(err);
			report.setStatus(Status.FAILURE);
			Date failureDate = dto.getStartDate() != null ? dto.getStartDate() : dto.getPreviousDate();
			reportCronFailure(failureDate,CronName.SET_CUSTOMER_BALANCE_SUM_BY_PROGRAM,err);
            
            throw new BizException("Error while updating customer balance.", e.getMessage());
        }finally {
        	long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(totalCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
        }

    }

    @Override
    public void downloadTransactionReport(HttpServletRequest request, HttpServletResponse response, Map<String, String> requestParams) {

        log.info("** Processing txn data for offline ");
        try {
            TransactionDTO dto = new TransactionDTO();
            dto.setDateRange(requestParams.get("dateRange"));
            dto.setProxyCardNo(requestParams.get("proxyCardNo"));
            dto.setCustomerMobile(requestParams.get("customerMobile"));
            dto.setProgramName(requestParams.get("programName"));
            dto.setDateRange(requestParams.get("dateRange"));
            String transactionList = requestParams.get("tranasctionTypes");
            List<TransactionTypes> transactionTypes = new ArrayList<>();
            if (StringUtils.isNotEmpty(transactionList)) {
                String[] transactionListSplit = transactionList.split(",");
                for (String eachObject : transactionListSplit) {
                    transactionTypes.add(TransactionTypes.valueOf(eachObject));
                }
            }
            dto.setTranasctionTypes(transactionTypes);
            requestParams.get("tranasctionTypes");
            String role = requestParams.get("role");

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
            log.info("** Filters before query are programName {}", dto.getProgramName() + ",Date{} " + startDate + " " + endDate + ", proxycardno{} " + dto.getProxyCardNo());
            List<Transaction> transactions = transactionRepository.findAll(transactionSpecification(dto));

            if (transactions != null)
                log.info("** Data fetched " + transactions.size());
            else
                log.info("** No DATA fetched. ");
            processTransactionDownload(transactions, response);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error While processing transaction offline. ", e.getMessage());
        }
    }


    @Override
    public Page<CustomerEODBalanceSumView> getCustomerEODBalanceSum(TransactionDTO dto) {


        log.info("** Fetch Customer Total Balance Details by filters : Date Filter {} |  Mobile {} | customer Name {} ", dto.getDateRange(), dto.getMobileNumber(),
                dto.getCustomerName());
        try {
            Sort sort = new Sort(Sort.Direction.DESC, "lastUpdatedAt");
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
            Page<CustomerEODBalanceSumView> customeTotalBalances = customerEODBalanceSumViewRepository.findAll(customerEODBalanceViewSumSpecification(dto), pageable);
            return customeTotalBalances;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while fetching transaction list. ", e.getMessage());
        }
    }

    private Specification<CustomerEODBalanceSumView> customerEODBalanceViewSumSpecification(TransactionDTO dto) {

        Specification<CustomerEODBalanceSumView> spec = (root, mq, mb) -> mb.equal(mb.literal(1), 1);

        if (StringUtils.isNotBlank(dto.getMobileNumber())) {
            log.info("** Filter ++ customer mobile {} ", dto.getMobileNumber());
            spec = spec.and((root, mq, mb) -> mb.equal(root.get("mobile"), dto.getMobileNumber()));
        }

        if (StringUtils.isNotBlank(dto.getCustomerName())) {
            log.info("** Filter ++ cutomer name {} ", dto.getCustomerName());
            spec = spec.and((root, mq, mb) -> mb.like(root.get("customerName"), dto.getCustomerName() + "%"));
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            log.info("** Filter added startdate : {} | endDate :  {} ", dto.getStartDate(), dto.getEndDate());
            spec = spec.and((root, mq, mb) -> mb.between(root.get("lastUpdatedAt"), dto.getStartDate(), dto.getEndDate()));
        }
        return spec;
    }

    @Override
    public Double getAnnualWalletCreditBalanceByCustomerForGPR(final String customerMobile, final TransactionTypes transactionType) {
        log.info("*** Inside get Annual Wallet Credit Balance by customerMobile :{}, and transactionType :{}", customerMobile, transactionType);
        final Calendar now = Calendar.getInstance();
        now.set(Calendar.DATE, now.get(Calendar.DATE) - 1);
        log.info("Year :{}", now.get(Calendar.YEAR));
        try {
            Double annualSumOfAmount = transactionRepository.getAnnualWalletFundAmountByCustomerForGPR(customerMobile, now.get(Calendar.YEAR), transactionType.getValue());
            if (annualSumOfAmount == null) {
                log.error("Annual Sum of amount is :{}", annualSumOfAmount);
                return 0.0;
            }
            return annualSumOfAmount;
        } catch (DataAccessException dae) {
            log.error("Exception occurred while fetching the records");
            log.error(dae.getMessage(), dae);
            return 0.0;
        }
    }

    @Override
    public Double getMonthlyWalletCreditBalanceByCustomerForGPR(final String customerMobile, final TransactionTypes transactionType) {
        log.info("*** Inside get Monthly Wallet Credit Balance by customerMobile :{}, and transactionType :{}", customerMobile, transactionType);
        final Calendar now = Calendar.getInstance();
        now.set(Calendar.DATE, now.get(Calendar.DATE) - 1);
        log.info("Year :{}, Month: {}", now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
        try {
            Double monthlySumOfAmount = transactionRepository.getMonthlyWalletFundAmountByCustomerForGPR(customerMobile, now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, transactionType.getValue());
            if (monthlySumOfAmount == null) {
                log.error("Monthly Sum of amount is :{}", monthlySumOfAmount);
                return 0.0;
            }
            return monthlySumOfAmount;
        } catch (DataAccessException dae) {
            log.error("Exception occurred while fetching the records");
            log.error(dae.getMessage(), dae);
            return 0.0;
        }
    }

    @Override
    public Double getCurrentBalanceByCustomerForGPR(final String customerMob) {
        log.info("*** Inside get customer balance by customerMob :{}", customerMob);
        try {
            Double currentBalance = eodBalanceRepository.findSumOfBalanceByMobileForGPR(customerMob);
            if (currentBalance == null) {
                log.error("currentBalance is :{}", currentBalance);
                return 0.0;
            }
            return currentBalance;
        } catch (DataAccessException dae) {
            log.error("Exception occurred while fetching the records");
            log.error(dae.getMessage(), dae);
            return 0.0;
        }
    }

    private void processTransactionDownload(List<Transaction> transactions, HttpServletResponse response2) {
        if (transactions instanceof List<?>) {
            String resultantFileName = "transaction-details-report.xlsx";
            log.info("--> Filename : " + resultantFileName + " Initiated..");
            final List<Transaction> transactionList = (List<Transaction>) transactions;
            if (transactionList.size() == 0) {
                throw new BizException("No Card Txn reports!");
            }
            final List<Map<String, String>> resultantData = new ArrayList<Map<String, String>>();
            final List<String> headers = new ArrayList<String>();
            headers.add("CREATED AT");
            headers.add("UPDATED AT");
            headers.add("PROGRAM");
            headers.add("CLIENT");
            headers.add("CUSTOMER NAME");
            headers.add("CUSTOMER MOBILE");
            headers.add("CUSTOMER EMAIL");
            headers.add("PROXY CARD NO");
            headers.add("MASK CARD NO");
            headers.add("CARD TYPE");
            headers.add("TRANSACTION TYPE");
            headers.add("TRANSACTION REF NO");
            headers.add("RETRIEVAL REF NO");
            headers.add("COMMENTS");
            headers.add("ORIGINAL TXN DATE");
            headers.add("ORIGINAL AMOUNT");
            headers.add("TRANSACTION AMOUNT");
            headers.add("BILLING AMOUNT");
            headers.add("CASHBACK AMOUNT");
            headers.add("CURRENT BALANCE");
            headers.add("DISTRIBUTOR");
            headers.add("RETAILER");
            headers.add("MERCHANT NAME");
            headers.add("POCKET");
            headers.add("STATUS");

            for (Transaction transaction : transactions) {
                final Map<String, String> dataMap = new HashMap<String, String>();
                final String transactionRefNo = StringUtils.isNotEmpty(transaction.getTransactionRefNo()) ? transaction.getTransactionRefNo() : "NA";
                final String program = StringUtils.isNotEmpty(transaction.getProgramName()) ? transaction.getProgramName() : "NA";
                final String clientId = StringUtils.isNotEmpty(transaction.getClientName()) ? transaction.getClientName() : "NA";
                final String customerName = StringUtils.isNotEmpty(transaction.getCustomerName()) ? transaction.getCustomerName() : "NA";
                final String customerMobile = StringUtils.isNotEmpty(transaction.getCustomerMobile()) ? transaction.getCustomerMobile() : "NA";
                final String customerEmail = StringUtils.isNotEmpty(transaction.getEmail()) ? transaction.getEmail() : "NA";
                final String proxyCardNo = StringUtils.isNotEmpty(transaction.getProxyCardNo()) ? transaction.getProxyCardNo() : "NA";
                final String maskCardNo = StringUtils.isNotEmpty(transaction.getMaskCardNumber()) ? transaction.getMaskCardNumber() : "NA";
                final String transactionAmt = transaction.getTransactionAmt() + "";
                final String retrievalRefNo = StringUtils.isNotEmpty(transaction.getRetrivalRefNo()) ? transaction.getRetrivalRefNo() : "NA";
                final String status = transaction.getStatus() + "";
                final String transactioncreatedAt = transaction.getTransactionCreatedAt() + "";
                final String updatedAt = transaction.getUpdatedAt() + "";
                final String transactionType = transaction.getTransactionType() + "";
                final String comments = StringUtils.isNotEmpty(transaction.getComments()) ? transaction.getComments() : "NA";
                final String billingAmt = transaction.getBillingAmt() + "";
                final String cashBack = transaction.getCashBackAmt() + "";
                final String currentBal = transaction.getCurrentBalance() + "";
                final String pocketName = transaction.getPocketName() != null ? transaction.getPocketName() : "NA";
                final String distributorName = StringUtils.isNotEmpty(transaction.getDistributorName()) ? transaction.getDistributorName() : "NA";
                final String agentName = StringUtils.isNotEmpty(transaction.getAgentName()) ? transaction.getAgentName() : "NA";
                final String cardType = StringUtils.isNotEmpty(transaction.getCardType()) ? transaction.getCardType() : "NA";
                final String merchantName = StringUtils.isNotEmpty(transaction.getMerchantName()) ? transaction.getMerchantName() : "NA";
                final String originalTxnDate = StringUtils.isNotEmpty(transaction.getOriginalTxnDate()) ? transaction.getOriginalTxnDate() : "NA";
                final String originalAmount = transaction.getOriginalAmount() + "";

                dataMap.put("CREATED AT", transactioncreatedAt);
                dataMap.put("UPDATED AT", updatedAt);
                dataMap.put("PROGRAM", program);
                dataMap.put("CLIENT", clientId);
                dataMap.put("CUSTOMER NAME", customerName);
                dataMap.put("CUSTOMER MOBILE", customerMobile);
                dataMap.put("CUSTOMER EMAIL", customerEmail);
                dataMap.put("PROXY CARD NO", proxyCardNo);
                dataMap.put("MASK CARD NO", maskCardNo);
                dataMap.put("CARD TYPE", cardType);
                dataMap.put("TRANSACTION TYPE", transactionType);
                dataMap.put("TRANSACTION REF NO", transactionRefNo);
                dataMap.put("RETRIEVAL REF NO", retrievalRefNo);
                dataMap.put("COMMENTS", comments);
                dataMap.put("ORIGINAL TXN DATE", originalTxnDate);
                dataMap.put("ORIGINAL AMOUNT", originalAmount);
                dataMap.put("TRANSACTION AMOUNT", transactionAmt);
                dataMap.put("BILLING AMOUNT", billingAmt);
                dataMap.put("CASHBACK AMOUNT", cashBack);
                dataMap.put("CURRENT BALANCE", currentBal);
                dataMap.put("DISTRIBUTOR", distributorName);
                dataMap.put("RETAILER", agentName);
                dataMap.put("MERCHANT NAME", merchantName);
                dataMap.put("POCKET", pocketName);
                dataMap.put("STATUS", status);
                resultantData.add(dataMap);
            }
            log.info("--> Headers for excel : " + headers + " |   DataMap : " + resultantData);
            CommonUtil.generateExcelSheet(headers, resultantData, resultantFileName, response2);
        }
    }

    @Override
    public void downloadCustomerBalance(HttpServletRequest request, HttpServletResponse response, String programUrl,
                                        Map<String, String> requestParams) {
        log.info("** Generate offline report for customer balance : ");

        try {
            String date = request.getParameter("dateRange");
            String customerName = request.getParameter("customerName");
            String customerHashId = request.getParameter("customerHashId");
            String mobileNo = request.getParameter("mobileNo");

            TransactionDTO dto = new TransactionDTO();
            dto.setDateRange(date);
            dto.setCustomerMobile(customerName);
            dto.setCustomerHashId(customerHashId);
            dto.setMobileNumber(mobileNo);

            Date startDate = null;
            Date endDate = null;

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

            List<CustomerEODBalanceView> customerBalances = customerEODBalanceViewRepository.findAll(customerEODBalanceViewSpecification(dto));
            processCustomerBalanceOffline(customerBalances, response);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while generating offline report. ", e.getMessage());
        }

    }


    @Override
    public void downloadCustomerSumBalance(HttpServletRequest request, HttpServletResponse response, String programUrl,
                                           Map<String, String> requestParams) {
        try {

            String date = request.getParameter("dateRange");
            String mobileNo = request.getParameter("mobileNo");

            TransactionDTO dto = new TransactionDTO();
            dto.setDateRange(date);
            dto.setMobileNumber(mobileNo);

            Date startDate = null;
            Date endDate = null;

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

            List<CustomerEODBalanceSumView> customerBalances = customerEODBalanceSumViewRepository.findAll(customerEODBalanceViewSumSpecification(dto));
            processCustomerSumBalanceOffline(customerBalances, response);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("Error while generating sum of eod balance for customer. ", e.getMessage());
        }
    }


    private void processCustomerSumBalanceOffline(List<CustomerEODBalanceSumView> customerBalances,
                                                  HttpServletResponse response) {

        if (customerBalances instanceof List<?>) {
            String resultantFileName = "Customer_EOD.xlsx";

            if (customerBalances.size() == 0) {
                throw new BizException("No data found!");
            }
            final List<Map<String, String>> resultantData = new ArrayList<Map<String, String>>();
            final List<String> headers = new ArrayList<String>();
            headers.add("Last Updated At");
            headers.add("Mobile");
            headers.add("Balance");

            for (CustomerEODBalanceSumView customerData : customerBalances) {
                final Map<String, String> dataMap = new HashMap<String, String>();

                final String lastUpdatedAt = StringUtils.isNotEmpty(String.valueOf(customerData.getLastUpdatedAt())) ? String.valueOf(customerData.getLastUpdatedAt()) : "NA";
                final String mobile = StringUtils.isNotEmpty(customerData.getMobile()) ? customerData.getMobile() : "NA";
                //final Double balance = customerData.getBalance();
                final Double gprBalance = customerData.getGprBalance();
                final Double gcBalance = customerData.getGcBalance();

                dataMap.put("Last Updated At", lastUpdatedAt);
                dataMap.put("Mobile", mobile);
                //dataMap.put("Balance", balance.toString());
                dataMap.put("gprBalance", gprBalance.toString());
                dataMap.put("gcBalance", gcBalance.toString());
                resultantData.add(dataMap);
            }
            CommonUtil.generateExcelSheet(headers, resultantData, resultantFileName, response);
        }

    }


    private void processCustomerBalanceOffline(List<CustomerEODBalanceView> customerBalances, HttpServletResponse response) {

        if (customerBalances instanceof List<?>) {
            String resultantFileName = "Customer_EOD.xlsx";

            if (customerBalances.size() == 0) {
                throw new BizException("No data found!");
            }
            final List<Map<String, String>> resultantData = new ArrayList<Map<String, String>>();
            final List<String> headers = new ArrayList<String>();
            headers.add("Last Updated At");
            headers.add("Customer Name");
            headers.add("Mobile");
            headers.add("Balance");
            headers.add("Program Name");
            headers.add("Program Type");
            headers.add("Program Plan");
            headers.add("Customer Hash ID");

            for (CustomerEODBalanceView customerData : customerBalances) {
                final Map<String, String> dataMap = new HashMap<String, String>();

                final String lastUpdatedAt = StringUtils.isNotEmpty(String.valueOf(customerData.getLastUpdatedAt())) ? String.valueOf(customerData.getLastUpdatedAt()) : "NA";
                final String customerName = StringUtils.isNotEmpty(customerData.getCustomerName()) ? customerData.getCustomerName() : "NA";
                final String mobile = StringUtils.isNotEmpty(customerData.getMobile()) ? customerData.getMobile() : "NA";
                final Double balance = customerData.getBalance();
                final String programName = StringUtils.isNotEmpty(customerData.getProgramName()) ? customerData.getProgramName() : "NA";
                final String programType = StringUtils.isNotEmpty(customerData.getProgramType()) ? customerData.getProgramType() : "NA";
                final ProgramPlans programPlan = customerData.getProgramPlan();
                final String customerHashId = StringUtils.isNotEmpty(customerData.getCustomerHashId()) ? customerData.getCustomerHashId() : "NA";

                dataMap.put("Last Updated At", lastUpdatedAt);
                dataMap.put("Customer Name", customerName);
                dataMap.put("Mobile", mobile);
                dataMap.put("Balance", balance.toString());
                dataMap.put("Program Name", programName);
                dataMap.put("Program Type", programType);
                dataMap.put("Program Plan", programPlan.getValue());
                dataMap.put("Customer Hash ID", customerHashId);
                resultantData.add(dataMap);
            }
            CommonUtil.generateExcelSheet(headers, resultantData, resultantFileName, response);
        }
    }

    @Override
    public List<String> getDistinctMobileListForGPR() {
        log.info("*** Get Dist Mobile for GPR - START");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
        return transactionRepository.getDistinctMobileListForGPR(cal.getTime());
    }


	@Override
	public ResponseEntity<?> storeCustomerTransactionBasedOnDateRange(TransactionDTO dto) {
		log.info("** StartDate : {} and EndDate : {}",dto.getStart(),dto.getEnd());
		long begin = System.currentTimeMillis();
		long rCount = 0l;
		CronReport report = new CronReport();
		report.setCreatedAt(new Date());
		report.setUpdatedAt(new Date());
		report.setCreatedBy("admin");
		report.setCronName(CronName.CUSTOMER_TRANSACTION_DATE_RANGE);
		try {
			Date previousDate = CommonUtil.getPastDateFromCurrentDate();
			log.info("** Previous date calculated by the system is : "+previousDate);
			String startDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getStart() + CommonUtil.startTime));
            Date start = new SimpleDateFormat("yyyy-MM-dd ").parse(startDate1);
            
            String endDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getEnd() + CommonUtil.endTime));
            Date end = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endDate1);
            if(end.after(previousDate)) {
            	log.error("End Date cannot be greater than Previous Date.");
            	throw new BizException("End Date cannot be greater than Previous Date.");
            }
            Date tempDate = start;
            while(!tempDate.after(end)) {
            	log.info("** Start ->  transaction Initiated for date : {} successfully. ",tempDate);
            	rCount = storeCustomerTransactionByDate(tempDate,rCount);
            	log.info("** end transaction fetched by date : {} successfully. ",tempDate);
            	tempDate = CommonUtil.addDays(tempDate, 1);
            }
            
	        
            report.setStatus(Status.SUCCESS);
            report.setComment("Txn Updated Succesfully from "+dto.getStart()+" to "+dto.getEnd()+" Ran by cron DateRange.");
			return new ResponseEntity<>("Txn Updated Successfully between "+dto.getStart()+"-"+dto.getEnd(),HttpStatus.OK);
			
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
			report.setComment(err);
			report.setStatus(Status.FAILURE);
			throw new BizException("Error While Fetching Transaction Based on Date Range. ",e.getMessage());
			
		}finally {
			
			long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(rCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
		}
	}
	
	private long storeCustomerTransactionByDate(Date currentDate,long rCount) {
		
		  try {
	            long begin = System.currentTimeMillis();
	            recordCount = 0l;
	            Date previousDate = currentDate;

	            String startDate = CommonUtil.getQuarterStartTime(Quater.FIRST_QUARTER, previousDate);
	            String endDate = CommonUtil.getQuarterEndTime(Quater.FIRST_QUARTER, previousDate);
	            ;
	            log.info("** Fetching transaction for Quarter 1, start {} end {} ", startDate, endDate);
	            rCount = CustomerTransactionByDate(startDate, endDate,rCount);
	            //Thread.sleep(5000);

	            startDate = CommonUtil.getQuarterStartTime(Quater.SECOND_QUARTER, previousDate);
	            endDate = CommonUtil.getQuarterEndTime(Quater.SECOND_QUARTER, previousDate);
	            log.info("** Fetching transaction for Quarter 2, start  {} end  {} ", startDate, endDate);
	            rCount = CustomerTransactionByDate(startDate, endDate,rCount);
	            //Thread.sleep(5000);

	            startDate = CommonUtil.getQuarterStartTime(Quater.THIRD_QUARTER, previousDate);
	            endDate = CommonUtil.getQuarterEndTime(Quater.THIRD_QUARTER, previousDate);
	            log.info("** Fetching transaction for Quarter 3, start {} end {} ", startDate, endDate);
	            rCount = CustomerTransactionByDate(startDate, endDate,rCount);
	            //Thread.sleep(5000);

	            startDate = CommonUtil.getQuarterStartTime(Quater.FOURTH_QUARTER, previousDate);
	            endDate = CommonUtil.getQuarterEndTime(Quater.FOURTH_QUARTER, previousDate);
	            log.info("** Fetching transaction for Quarter 4, start {} end {} ", startDate, endDate);
	            rCount =  CustomerTransactionByDate(startDate, endDate,rCount);
	            //Thread.sleep(5000);

	            long endTime = System.currentTimeMillis();
	            double time = (endTime - begin) / 1000;
	            log.info("** Time Required to fetch {} records is: {}s ", recordCount, time);
	            	
	            return rCount;

	        } catch (Exception e) {
	            log.error(e.getMessage(), e);
	            throw new BizException("Error while storing cutomer txn. ", e.getMessage());
	        }
	}
	
	

	@Override
	public ResponseEntity<?> setUpEODByDate(TransactionDTO dto) {
		log.info("** StartDate : {} and EndDate : {}",dto.getStart(),dto.getEnd());
		long begin = System.currentTimeMillis();
		long rCount = 0l;
		CronReport report = new CronReport();
		report.setCreatedAt(new Date());
		report.setUpdatedAt(new Date());
		report.setCreatedBy("admin");
		report.setCronName(CronName.SET_EOD_INCREMENTAL);
		try {
			Date previousDate = CommonUtil.getPastDateFromCurrentDate();
			String startDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getStart() + CommonUtil.startTime));
            Date start = new SimpleDateFormat("yyyy-MM-dd ").parse(startDate1);
            
            String endDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getEnd() + CommonUtil.endTime));
            Date end = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endDate1);
            if(end.after(previousDate)) {
            	log.error("End Date cannot be greater than Previous Date.");
            	throw new BizException("End Date cannot be greater than Previous Date.");
            }
            Date tempDate = start;
            while(!tempDate.after(end)) {
            	log.info("** Start ->  EOD Initiated for date : {} . ",tempDate);
            	rCount = setUpEODBalanceIncrementalByDateRange(tempDate,rCount);
            	log.info("** end EOD fetched by date : {} successfully. ",tempDate);
            	tempDate = CommonUtil.addDays(tempDate, 1);
            }
            report.setStatus(Status.SUCCESS);
            report.setComment("EOD Balance Updated Successfully from "+dto.getStart()+" to "+dto.getEnd());
			return new ResponseEntity<>("EOD Balance Updated Successfully between "+dto.getStart()+"-"+dto.getEnd(),HttpStatus.OK);
            
			
		}catch(Exception e) {
			
			log.error(e.getMessage(),e);
			String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
			report.setComment(err);
			report.setStatus(Status.FAILURE);
			throw new BizException("Error While configuring EOD Balance Based on Date Range. ",e.getMessage());
		}finally {
			
			long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(rCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
		}
		
		
	}
	
	
	private long setUpEODBalanceIncrementalByDateRange(Date currentDate,long recordCount) {
		log.info("Inside Setup EOD Balance Data for Current Day");
        try {
            Date pastDayDate = currentDate;

            List<String> cardWithPocketList = transactionRepository.getGroupProxyCardByDate(pastDayDate);
            if (cardWithPocketList.isEmpty()) {
                log.error("No data found");
                throw new BizException("No data found");
            }

            int txnCount = 0, txnCountTillNow = 0;
            for (String proxyCardNo : cardWithPocketList) {
                try {
                    log.info("finding txn list for proxyCardNo :{}", proxyCardNo);
                    if (proxyCardNo != null) {
                        List<Transaction> txnViewList = transactionRepository.getAllByProxyCard(proxyCardNo, pastDayDate);
                        log.info("TxnViewList Size :{}", txnViewList.size());

                        if (!txnViewList.isEmpty()) {
                            List<Transaction> pocketBasedTxnList = txnViewList.stream()
                                    .filter(distinctByKey(p -> p.getPocketName()))
                                    .collect(Collectors.toList());
                            // CUST A--> POCKET FOOD-10k,DEFAULT-10k ,CUST B, CUST C ;
                            double sumOfAllPocketBalance = pocketBasedTxnList.stream().mapToDouble(s -> s.getCurrentBalance()).sum();
                            log.info("Sum of all pocketBalances :{}", sumOfAllPocketBalance);
                            Transaction transactionView = pocketBasedTxnList.get(0);

                            /*Save/Update the EOD Data*/
                            saveOrUpdateEodData(sumOfAllPocketBalance, transactionView);

                            txnCount++;
                            recordCount++;
                            log.info("txnCount : {}", txnCount);
                            txnCountTillNow++;
                            log.info("Txn Count till now :{}", txnCountTillNow);
                            if (txnCount > 100) {
                                log.info("Txn count is more then :{}, let's sleep for 5sec", txnCount);
                                txnCount = 0;
                                Thread.sleep(5000);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while set or update the EOD Data on Daily Basis");
                    log.error(e.getMessage(), e);
                }
            }

            log.info("Setup EOD Balance Data for Prev Day completed!");
            //updateCustomerBalance();
            return recordCount;
        } catch (Exception e) {
            log.error("Exception occurred while set or update the EOD Data or setting customer balance on Daily Basis");
            log.error(e.getMessage(), e);
            return recordCount;
        }
		
	}


	@Override
	public ResponseEntity<?> setUpCustomerBalanceByDate(TransactionDTO dto) {
		log.info("** StartDate : {} and EndDate : {}",dto.getStart(),dto.getEnd());
		long begin = System.currentTimeMillis();
		long rCount = 0l;
		CronReport report = new CronReport();
		report.setCreatedAt(new Date());
		report.setUpdatedAt(new Date());
		report.setCreatedBy("admin");
		report.setCronName(CronName.SET_CUSTOMER_BALANCE_BY_PROGRAM);
		try {
			
			Date previousDate = CommonUtil.getPastDateFromCurrentDate();
			String startDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getStart() + CommonUtil.startTime));
            Date start = new SimpleDateFormat("yyyy-MM-dd ").parse(startDate1);
            
            String endDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getEnd() + CommonUtil.endTime));
            Date end = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endDate1);
            if(end.after(previousDate)) {
            	log.error("End Date cannot be greater than Previous Date.");
            	throw new BizException("End Date cannot be greater than Previous Date.");
            }
            Date tempDate = start;
            while(!tempDate.after(end)) {
            	log.info("** Start ->  Customer Program Level Balance Update for date : {} . ",tempDate);
            	rCount = setUpCustomerBalanceByDateRange(tempDate,rCount);
            	log.info("** end Customer Program Level Balance Update : {} successfully. ",tempDate);
            	tempDate = CommonUtil.addDays(tempDate, 1);
            }
            report.setStatus(Status.SUCCESS);
            report.setComment("Customer Program Level Balance Updated Successfully from "+dto.getStart()+" to "+dto.getEnd());
			return new ResponseEntity<>("Customer Program Level Balance Updated Successfully between "+dto.getStart()+"-"+dto.getEnd(),HttpStatus.OK);
			
			
		}catch(Exception e) {
			
			log.error(e.getMessage(),e);
			String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
			report.setComment(err);
			report.setStatus(Status.FAILURE);
			throw new BizException("Error While Setting Up Customer Balance By Program on Date Range. ",e.getMessage());
			
		}finally {
			
			long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(rCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
		}
	}
	
	private long setUpCustomerBalanceByDateRange(Date currentDate,long recordCount) {
		log.info("** Update Customer Balance Date Wise. ");
        long begin = System.currentTimeMillis();
        int count = 0;
        long totalCount = 0l;
        try {
            Date previousDate = currentDate;
            log.info("** Previous Date {}", previousDate);
            List<String> customerHashIds = eodBalanceRepository.getGroupByCustomerHash();
            log.info("** Total customers are {} ", customerHashIds.size());
            for (String customerHashId : customerHashIds) {
                if (customerHashId != null) {
                    Double balance = eodBalanceRepository.findSumOfBalanceGroupByCustomer(customerHashId);
                    CustomerEODBalance customerEODBalance = new CustomerEODBalance();
                    customerEODBalance.setCreatedAt(new Date());
                    customerEODBalance.setUpdatedAt(new Date());
                    customerEODBalance.setEodCreatedAt(previousDate);
                    customerEODBalance.setCustomerHashId(customerHashId);
                    customerEODBalance.setBalance(balance);
                    customerEODBalanceRepository.save(customerEODBalance);
                    count++;
                    totalCount++;
                    recordCount++;
                    if (count == 50) {
                        log.info("** Thread sleep for 3s");
                        Thread.sleep(3000);
                        count = 0;
                    }
                }
            }
            long end = System.currentTimeMillis();
            long timeTaken = (end - begin) / 1000;
            log.info("** Total customer-counter :{} , timeTaken: {}s ", totalCount, timeTaken);
            //updateCustomerBalanceSum();
            return recordCount;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return recordCount;
        }
		
	}


	/**
	 * @param : start and End is mandatory in the input param(TransactionDTO) 
	 * 	 For example: "start":"21/07/2021" ; "end":"01/09/2021";
	 */
	@Override
	public ResponseEntity<?> setUpCustomerBalanceSumByDate(TransactionDTO dto) {
		log.info("** StartDate : {} and EndDate : {}",dto.getStart(),dto.getEnd());
		long begin = System.currentTimeMillis();
		long rCount = 0l;
		CronReport report = new CronReport();
		report.setCreatedAt(new Date());
		report.setUpdatedAt(new Date());
		report.setCreatedBy("admin");
		report.setCronName(CronName.SET_CUSTOMER_BALANCE_SUM_BY_PROGRAM);
		try {
			
			Date previousDate = CommonUtil.getPastDateFromCurrentDate();
			String startDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getStart() + CommonUtil.startTime));
            Date start = new SimpleDateFormat("yyyy-MM-dd ").parse(startDate1);
            
            String endDate1 = CommonUtil.dateFormate.format(CommonUtil.dateFormate2.parse(dto.getEnd() + CommonUtil.endTime));
            Date end = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endDate1);
            if(end.after(previousDate)) {
            	log.error("End Date cannot be greater than Previous Date.");
            	throw new BizException("End Date cannot be greater than Previous Date.");
            }
            Date tempDate = start;
            while(!tempDate.after(end)) {
            	log.info("** Start ->  Customer Program Level Balance Sum Update for date : {} . ",tempDate);
            	rCount = updateCustomerBalanceSumByDateRange(tempDate,rCount);
            	log.info("** end Customer Program Level Balance Sum Update : {} successfully. ",tempDate);
            	tempDate = CommonUtil.addDays(tempDate, 1);
            }
            report.setStatus(Status.SUCCESS);
            report.setComment("Customer Program Level Balance Sum Updated Successfully from "+dto.getStart()+" to "+dto.getEnd());
			return new ResponseEntity<>("Customer Program Level Balance Sum Updated Successfully between "+dto.getStart()+"-"+dto.getEnd(),HttpStatus.OK);
			
			
		}catch(Exception e) {
			
			log.error(e.getMessage(),e);
			String err =  (e.getMessage() != null || e.getMessage() != "") && (e.getMessage().length() > 255) ? e.getMessage().substring(0, 244):e.getMessage();
			report.setComment(err);
			report.setStatus(Status.FAILURE);
			throw new BizException("Error While Setting Up Customer Balance Sum By Program on Date Range. ",e.getMessage());
			
		}finally {
			
			long endTime = System.currentTimeMillis();
	        double time = (endTime - begin) / 1000;
	        report.setExecutionTime(time);
	        report.setRecordCount(rCount);
			cronReportRepository.save(report);
			log.info("Executing finally block");
		}
	}
	
	private long updateCustomerBalanceSumByDateRange(Date currentDate,long recordCount) {

        log.info("** Update Customer Total/Sum Balance Date Wise. ");
        long begin = System.currentTimeMillis();
        int count = 0;
        long totalCount = 0l;
        double GPR_Balance;
        double GC_Balance;
        try {
            Date previousDate = currentDate;
            log.info("** Previous Date {}", previousDate);
            List<String> customerMobile = eodBalanceRepository.getGroupByCustomerMobile();
            log.info("** Total customers mobiles are {} ", customerMobile.size());
            for (String mobile : customerMobile) {
                if (mobile != null) {
                    //Double balance = eodBalanceRepository.findSumOfBalanceGroupByMobile(mobile);
                    GPR_Balance = eodBalanceRepository.findSumOfGPRBalanceGroupByMobile(mobile) == null ? 0 : eodBalanceRepository.findSumOfGPRBalanceGroupByMobile(mobile);
                    GC_Balance = eodBalanceRepository.findSumOfGCBalanceGroupByMobile(mobile) == null ? 0 : eodBalanceRepository.findSumOfGCBalanceGroupByMobile(mobile);
                    CustomerEODSumBalance customerEODSumBalance = new CustomerEODSumBalance();
                    customerEODSumBalance.setCreatedAt(new Date());
                    customerEODSumBalance.setUpdatedAt(new Date());
                    customerEODSumBalance.setEodCreatedAt(previousDate);
                    customerEODSumBalance.setCustomerMobile(mobile);
                    ;
                    customerEODSumBalance.setGprBalance(GPR_Balance);
                    customerEODSumBalance.setGcBalance(GC_Balance);
                    customerEODBalanceSumRepository.save(customerEODSumBalance);
                    count++;
                    totalCount++;
                    recordCount++;
                    if (count == 50) {
                        log.info("** Thread sleep for 3s");
                        Thread.sleep(3000);
                        count = 0;
                    }
                }
            }
            long end = System.currentTimeMillis();
            long timeTaken = (end - begin) / 1000;
            log.info("** Total customer-counter :{} , timeTaken: {}s ", totalCount, timeTaken);
            return recordCount;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return recordCount;
        }

    }
	
	
	private boolean hasFailedRecord(CronName cronName,TransactionDTO dto) {
		log.info("** Checking if any cron has failed for cron: ",cronName);
		CronFailure cronFailureEntity = cronFailureRepository.findByStatusAndCronName(Status.FAILURE,cronName);
		boolean hasRecord = cronFailureEntity == null ? false:true;
		if(hasRecord) {
			dto.setStartDate(cronFailureEntity.getFailureDate());
		}
		log.info("** {} has records  ? : {} ",cronName,hasRecord);
		return hasRecord;
	}
	
	/**
	 * 
	 * @param dto : StartDate is mandatory Data type is java.util.Date
	 */
	private void processForFailTxn(TransactionDTO dto) {
		log.info("** processing for failed case at processForFailTxn() with startDate as : "+dto.getStartDate());
		try {
			Date endDate = CommonUtil.getPastDateFromCurrentDate();
			dto.setEndDate(endDate);
			dto.setStart(CommonUtil.dateFormate3.format(dto.getStartDate()));
			dto.setEnd(CommonUtil.dateFormate3.format(dto.getEndDate()));
			log.info("** Calling storeCustomerTransactionBasedOnDateRange() ");
			storeCustomerTransactionBasedOnDateRange(dto);
			log.info("** Cron was successful hence marking cronfailure as success. ");
			markCronFailureSuccess(CronName.CUSTOMER_TRANSACTION);
			
			
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException(e.getMessage());
		}
	}
	
	private void reportCronFailure(Date failureDate,CronName cronName, String failureReason) {
		CronFailure cronFailureEntity = cronFailureRepository.findByStatusAndCronName(Status.FAILURE,cronName);
		if(cronFailureEntity == null) {
			cronFailureEntity = new CronFailure();
			cronFailureEntity.setCronName(cronName);
			cronFailureEntity.setFailReason(failureReason);
			cronFailureEntity.setFailureDate(failureDate);
			cronFailureEntity.setStatus(Status.FAILURE);
			cronFailureRepository.save(cronFailureEntity);
		}
		log.info("Failure report added for {} ",cronName);
	}
	
	private void markCronFailureSuccess(CronName cronName) {
		CronFailure cronFailureEntity = cronFailureRepository.findByStatusAndCronName(Status.FAILURE,cronName);
		cronFailureEntity.setStatus(Status.SUCCESS);
		cronFailureRepository.save(cronFailureEntity);
	}
	
	private void processForFailCaseEODBalance(TransactionDTO dto) {
		log.info("** processing for failed case at processForFailCaseEODBalance() with startDate as : "+dto.getStartDate());
		try {
			Date endDate = CommonUtil.getPastDateFromCurrentDate();
			dto.setEndDate(endDate);
			dto.setStart(CommonUtil.dateFormate3.format(dto.getStartDate()));
			dto.setEnd(CommonUtil.dateFormate3.format(dto.getEndDate()));
			log.info("** Calling setUpEODByDate() ");
			setUpEODByDate(dto);
			log.info("** Cron was successful hence marking cronfailure as success. ");
			markCronFailureSuccess(CronName.SET_EOD_INCREMENTAL);
			
			
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException(e.getMessage());
		}
	}
	

    private void processForFailCaseCustomerProgramBalance(TransactionDTO dto) {
    	log.info("** processing for failed case at processForFailCaseCustomerProgramBalance() with startDate as : "+dto.getStartDate());
		try {
			Date endDate = CommonUtil.getPastDateFromCurrentDate();
			dto.setEndDate(endDate);
			dto.setStart(CommonUtil.dateFormate3.format(dto.getStartDate()));
			dto.setEnd(CommonUtil.dateFormate3.format(dto.getEndDate()));
			log.info("** Calling setUpCustomerBalanceByDate() ");
			setUpCustomerBalanceByDate(dto);
			log.info("** Cron was successful hence marking cronfailure as success. ");
			markCronFailureSuccess(CronName.SET_CUSTOMER_BALANCE_BY_PROGRAM);
			
			
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException(e.getMessage());
		}
		
	}
}
