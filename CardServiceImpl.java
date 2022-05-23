package com.zokudo.sor.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.zokudo.sor.enums.*;
import com.zokudo.sor.util.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zokudo.sor.dto.ApiError;
import com.zokudo.sor.dto.CardDTO;
import com.zokudo.sor.entities.CardView;
import com.zokudo.sor.entities.Cards;
import com.zokudo.sor.exceptions.BizException;
import com.zokudo.sor.repositories.CardRepository;
import com.zokudo.sor.repositories.CardViewRepository;
import com.zokudo.sor.service.CardService;
import com.zokudo.sor.util.CommonUtil;
import com.zokudo.sor.util.UrlMetaData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CardServiceImpl implements CardService {

	private final CardRepository cardRepository;
	private long recordCount;
	private final UrlMetaData urlMetaData;
	private final Client client;
	private final String applicationLevelUserName;
    private final String applicationLevelUserPassword;
    private final CardViewRepository cardviewRepository;
	private final SecurityUtil securityUtil;
	
	@Autowired
	public CardServiceImpl(
			final CardRepository cardRepository,
			final UrlMetaData urlMetaData,
			final Client client,
			@Value("${applicationLevel.user.name}") String applicationLevelUserName,
			@Value("${applicationLevel.user.password}") String applicationLevelUserPassword,
			final CardViewRepository cardviewRepository, SecurityUtil securityUtil) {
		this.cardRepository = cardRepository; 
		this.urlMetaData = urlMetaData;
		this.client = client;
		this.applicationLevelUserName = applicationLevelUserName;
		this.applicationLevelUserPassword = applicationLevelUserPassword;
		this.cardviewRepository = cardviewRepository;
		this.securityUtil = securityUtil;
	}
	@Override
	public void issueCardToCustomer() {
		try {
			
			long begin = System.currentTimeMillis();
			recordCount=0l;
			Date previousDate = CommonUtil.getPastDateFromCurrentDate();
			
			String startDate = getQuarterStartTime(Quater.FIRST_QUARTER,previousDate);
			String endDate = getQuarterEndTime(Quater.FIRST_QUARTER,previousDate);;
			log.info("** Fetching card details for Quarter 1, start {} end {} ",startDate,endDate);
			CustomerCardByDate(startDate,endDate);
			Thread.sleep(5000);
			
			startDate = getQuarterStartTime(Quater.SECOND_QUARTER,previousDate);
			endDate = getQuarterEndTime(Quater.SECOND_QUARTER,previousDate);
			log.info("** Fetching card details for Quarter 2, start  {} end  {} ",startDate,endDate);
			CustomerCardByDate(startDate,endDate);
			Thread.sleep(5000);
			
			startDate = getQuarterStartTime(Quater.THIRD_QUARTER,previousDate);
			endDate = getQuarterEndTime(Quater.THIRD_QUARTER,previousDate);
			log.info("** Fetching card details for Quarter 3, start {} end {} ",startDate,endDate);
			CustomerCardByDate(startDate,endDate);
			Thread.sleep(5000);
			
			startDate = getQuarterStartTime(Quater.FOURTH_QUARTER,previousDate);
			endDate = getQuarterEndTime(Quater.FOURTH_QUARTER,previousDate);
			log.info("** Fetching card details for Quarter 4, start {} end {} ",startDate,endDate);
			CustomerCardByDate(startDate,endDate);
			Thread.sleep(5000);
			
			long endTime = System.currentTimeMillis();
			double time = (endTime-begin)/1000;
	        log.info("** Time Required to fetch {} records is: {}s ",recordCount,time);
			
			
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException("Error while issue card to customer {} ",e.getMessage()); 
		}
	}
	
	@Override
	public Page<CardView> getCardList(CardDTO dto) {
		log.info("** Fetch Card Details by filters : Date Filter {}, ProxyNumber {} ",dto.getDateRange(),dto.getProgramName());
		try {
			Sort sort = new Sort(Sort.Direction.DESC, "orgCreatedAt");
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
			Page<CardView> cards = cardviewRepository.findAll(cardSpecification(dto),pageable);
			return cards;
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException("Error while fetching card details. "+e.getMessage());
		}
	}

	@Override
	public void downloadCardList(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams) throws Exception {
		CardDTO dto =new CardDTO();
		dto.setDateRange(requestParams.get("dateRange"));
		dto.setProgramName(requestParams.get("programName"));
		dto.setProxyNumber(requestParams.get("proxyCardNo"));
		dto.setMobile(requestParams.get("mobileNumber"));
		dto.setCardTypeFilter(requestParams.get("cardType"));
		dto.setProgramPlanFilter(requestParams.get("programplan"));
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
		final List<CardView> cardViewList= cardviewRepository.findAll(cardSpecification(dto));
		if (cardViewList instanceof List<?>) {
			String resultantFileName = "Card_List.xlsx";
			if (cardViewList.size() == 0) {
				throw new BizException("No Card data found!");
			}
			final List<Map<String, String>> resultantData = new ArrayList<Map<String, String>>();
			final List<String> headers = new ArrayList<String>();
			headers.add("CREATED DATE");
            headers.add("CUSTOMER MOBILE");
            headers.add("CUSTOMER NAME");
            headers.add("PROXY NUMBER");
			headers.add("CARD TYPE");
			headers.add("EXPIRY DATE");
			headers.add("PROGRAM NAME");
			headers.add("PROGRAM PLAN");
			headers.add("STATUS");
			for (CardView cardsData : cardViewList) {
				final Map<String, String> dataMap = new HashMap<String, String>();
				final String orgcreatedDate = StringUtils.isNotEmpty(String.valueOf(cardsData.getOrgCreatedAt())) ? String.valueOf(cardsData.getOrgCreatedAt()) : "NA";
				final String Mobile =StringUtils.isNotEmpty(cardsData.getMobile())?cardsData.getMobile():"NA";
				final String CustomerName =StringUtils.isNotEmpty(cardsData.getCustomerName())?cardsData.getCustomerName():"NA";
				final String proxyNo = StringUtils.isNotEmpty(cardsData.getProxyNumber()) ? cardsData.getProxyNumber() : "NA";
				final String cardTypeValue = StringUtils.isNotEmpty(cardsData.getCardType().getValue()) ? cardsData.getCardType().getValue() : "NA";
				final String expiryDate =StringUtils.isNotEmpty(cardsData.getExpiryDate())?cardsData.getExpiryDate():"NA";
                final String programName =StringUtils.isNotEmpty(cardsData.getProgramName())?cardsData.getProgramName():"NA";
                final String programPlan =StringUtils.isNotEmpty(cardsData.getProgramPlan().getValue())?cardsData.getProgramPlan().getValue():"NA";
                final String stat = StringUtils.isNotEmpty(cardsData.getCardStatus().getValue()) ? cardsData.getCardStatus().getValue() : "NA";

				dataMap.put("CREATED DATE", orgcreatedDate);
                dataMap.put("CUSTOMER MOBILE", Mobile);
				dataMap.put("CUSTOMER NAME", CustomerName);
				dataMap.put("PROXY NUMBER", proxyNo);
				dataMap.put("CARD TYPE", cardTypeValue);
				dataMap.put("EXPIRY DATE",expiryDate);
				dataMap.put("PROGRAM NAME",programName);
				dataMap.put("PROGRAM PLAN", programPlan);
				dataMap.put("STATUS", stat);
				resultantData.add(dataMap);
			}
			CommonUtil.generateExcelSheet(headers, resultantData, resultantFileName, response);
		}
	}

	private void CustomerCardByDate(String startDate, String endDate) {
		
		
		final JSONObject requestParameters = new JSONObject();
		try {
			requestParameters.put("startDate", startDate);
	        requestParameters.put("endDate", endDate);
	        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
	        headerMap.add("Authorization", securityUtil.getAuthorizationHeader());
	        headerMap.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
	        String strURl = urlMetaData.GET_CUSTOMER_CARD_SERVICE;

	        Response clientResponse = client.target(strURl)
	                .request()
	                .headers(headerMap)
	                .post(Entity.entity(requestParameters.toString(), MediaType.APPLICATION_JSON_VALUE));

	        String transactionDetails = clientResponse.readEntity(String.class);
	        if (clientResponse.getStatus() != 200)
	            throw new BizException(BizErrors.DATA_NOT_FOUND.getValue(), "Unable to fetch Transaction list!");
	        
	        JSONArray  cardObj = new JSONArray(transactionDetails);
	        log.info("** Response recieved: WE have {} pages of records. ",cardObj.length());
	        System.out.println(cardObj);
	        for(int i=0 ;i < cardObj.length();i++) {
	        	JSONObject cardsContent = cardObj.getJSONObject(i);
	        	JSONArray cards = cardsContent.getJSONArray("content");
		        
		        ObjectMapper mapper = new ObjectMapper();
		        List<CardDTO> cardDTO = mapper.readValue(cards.toString(), new TypeReference<List<CardDTO>>(){});
		        processEachTransaction(cardDTO);
		        log.info("Thread Sleep for 10s. ");
		        Thread.sleep(10000);
	        }
	         

		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException("Error while customer transaction by date. {} "+e.getMessage()); 
		}
		
	}
	private void processEachTransaction(List<CardDTO> cardList) throws InterruptedException {
		
		try {
			long saveCount = 0l;
			log.info("** Storing total {} records",cardList.size());
			for (CardDTO dto : cardList) {
				
				Cards cards = new Cards();
				log.info(" Saving card, with cardhashid {}, proxynumber {}, customerHashId {} ",dto.getCardHashId(),dto.getProxyNumber(),
						dto.getCustomerHashId());
				cards.setCustomerHashId(dto.getCustomerHashId());
				cards.setProxyNumber(dto.getProxyNumber());
				cards.setCardHashId(dto.getCardHashId());
				cards.setCardStatus(dto.getCardStatus());
				cards.setClientId(dto.getClientId());
				cards.setMaskCardNumber(dto.getMaskCardNumber());
				cards.setExpiryDate(dto.getExpiryDate());
				cards.setFeeDeduction(dto.isFeeDeduction());
				cards.setCardType(dto.getCardType());
				cards.setCreatedAt(dto.getCreatedAt());
				cards.setOrgCreatedAt(dto.getCreatedAt());
				cards.setUpdatedAt(dto.getUpdatedAt());
				cards.setProgramId(dto.getProgramId());
				cards.setProgramPlan(dto.getProgramPlan());
				cards.setActivationCode(dto.getActivationCode());
				cards.setCardActivationStatus(dto.getCardActivationStatus());
				
				
				cardRepository.save(cards);
				saveCount++;
				if(saveCount % 20 == 0) {
					log.info("** Record save count {} , pausing process for 5s ",saveCount);
					Thread.sleep(5000);
				}
			} 
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException("Error while storing card. ",e.getMessage());
		}
	}
	
	
	/**
	 * This method gives quater start date-time based on quater enum in String.
	 * For Example : If its FIRST_QUATER of 29/04/2021 then it must return 29/04/2021 00:00:00 " 
	 * @param Quater
	 * @return
	 * @throws ParseException 
	 */
	private String getQuarterStartTime(Quater quarter,Date previousDate) throws ParseException {
		log.info("** Previous Date params {}",previousDate);
		String parsedPreviousDate = CommonUtil.dateFormate_yyyy_MM_dd.format(previousDate);
		log.info("** Date after formatting {} ",parsedPreviousDate);
		String quarterDateTime;
		if(quarter == Quater.FIRST_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FIRST_QUATER_START_TIME));
			return quarterDateTime;
		}
		if(quarter == Quater.SECOND_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.SECOND_QUATER_START_TIME));
			return quarterDateTime;
		}
		if(quarter == Quater.THIRD_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.THIRD_QUATER_START_TIME) );
			return quarterDateTime;
		}
		if(quarter == Quater.FOURTH_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FOURTH_QUATER_START_TIME));
			return quarterDateTime;
		}
		throw new BizException("Invalid Quater Input");
	}
	
	
	/**
	 * This method gives quater end date-time based on quater enum in String.
	 * For Example : If its FIRST_QUATER of 29/04/2021 then it must return 29/04/2021 05:59:59 " 
	 * @param Quater
	 * @return
	 * @throws ParseException 
	 */
	private String getQuarterEndTime(Quater quarter,Date previousDate) throws ParseException {
		String parsedPreviousDate = CommonUtil.dateFormate_yyyy_MM_dd.format(previousDate);
		String quarterDateTime;
		if(quarter == Quater.FIRST_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FIRST_QUATER_END_TIME));
			return quarterDateTime;
		}
		if(quarter == Quater.SECOND_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.SECOND_QUATER_END_TIME));
			return quarterDateTime;
		}
		if(quarter == Quater.THIRD_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.THIRD_QUATER_END_TIME));
			return quarterDateTime;
		}
		if(quarter == Quater.FOURTH_QUARTER) {
			quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FOURTH_QUATER_END_TIME));
			return quarterDateTime;
		}
		throw new BizException("Invalid Quater Input");
	}
	private Specification<CardView> cardSpecification(CardDTO dto){
		Specification<CardView> spec  = (CardView, mq, mb) -> mb.equal(mb.literal(1), 1);
		try {
			if(StringUtils.isNotBlank(dto.getProxyNumber())) {
				log.info("** Filter added proxynumber {} ",dto.getProxyNumber());
				spec = spec.and((root,mq,mb) -> mb.equal(root.get("proxyNumber"), dto.getProxyNumber()));
			}
			if(StringUtils.isNotBlank(dto.getProgramName())) {
				log.info("** Filter added program name {} ",dto.getProgramName());
				spec = spec.and((root,mq,mb) -> mb.like(root.get("programName"), dto.getProgramName()+"%"));
			}
			if(StringUtils.isNotBlank(dto.getMobile())) {
				log.info("** Filter added customer mobile {} ",dto.getMobile());
				spec = spec.and((root,mq,mb) -> mb.equal(root.get("mobile"), dto.getMobile()));
			}
			if(dto.getStartDate() != null) {
				log.info("** Filter added startdate : {} | endDate :  {} ",dto.getStartDate(),dto.getEndDate());
				spec = spec.and((root,mq,mb) -> mb.between(root.get("orgCreatedAt"), dto.getStartDate(),dto.getEndDate()));
			}
			if(StringUtils.isNotBlank(dto.getStatus())) {
				log.info("** Filter added card status ",dto.getStatus());
				Status status = Status.valueOf(dto.getStatus());
				spec = spec.and((root,mq,mb) -> mb.equal(root.get("cardStatus"), status));
			}

			if(StringUtils.isNotBlank(dto.getCardTypeFilter())) {
				CardType cardType= CardType.valueOf(dto.getCardTypeFilter());
				log.info("** Filter added card type {} ",dto.getCardType());
				spec = spec.and((root,mq,mb) -> mb.equal(root.get("cardType"), cardType));
			}

			if(StringUtils.isNotBlank(dto.getProgramPlanFilter())) {
				ProgramPlans programPlans =ProgramPlans.valueOf(dto.getProgramPlanFilter());
				log.info("** Filter added program plan {} ",dto.getProgramPlan());
				spec = spec.and((root,mq,mb) -> mb.equal(root.get("programPlan"), programPlans));
			}


		}catch(Exception e) {
			throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "Internal server error. Please try again after sometimes");
		}
		return spec;
	}
	
	
	@Override
	public ResponseEntity<?> updateCardStatus(CardDTO dto) {
		
		 log.info("inside card update status for card hashID: {} with status {} ", dto.getCardHashId(),dto.getStatus());
	        try {
	        	Cards card = cardRepository.findByCardHashId(dto.getCardHashId());
	            if (card == null) {
	                log.error("No cards found with this  card hashID: {}", dto.getCardHashId());
	                throw new BizException("No card found with this card hash id " + dto.getCardHashId());
	            }

	            if (card != null) {
	            	CardStatus status = CardStatus.valueOf(dto.getStatus()); 
	            	card.setCardStatus(status);
	            	cardRepository.save(card);
	            }
	        } catch (Exception e) {
	            log.error("Error while updating card status " + e.getMessage(), e);
	            throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "error while updating card status");
	        }
	        ApiError response = new ApiError(HttpStatus.OK, "Card Status Updated Successfully!");
	        return new ResponseEntity<>("Card Status Updated Successfully!", HttpStatus.OK);
	}

}
