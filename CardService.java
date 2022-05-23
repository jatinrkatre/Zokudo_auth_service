package com.zokudo.sor.service;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.zokudo.sor.dto.CardDTO;
import com.zokudo.sor.entities.CardView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface CardService {

	public void issueCardToCustomer();
	
	public Page<CardView> getCardList(CardDTO dto);
	void downloadCardList(HttpServletRequest request, HttpServletResponse response, String programUrl, Map<String, String> requestParams) throws Exception;
	
	public ResponseEntity<?> updateCardStatus(CardDTO dto);
}
