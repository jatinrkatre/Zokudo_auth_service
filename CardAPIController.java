package com.zokudo.sor.controller;

import com.zokudo.sor.exceptions.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zokudo.sor.dto.CardDTO;
import com.zokudo.sor.entities.CardView;
import com.zokudo.sor.service.CardService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("{programUrl}/api/v1/cards")
public class CardAPIController {
	
	private final CardService cardService;
	
	@Autowired
	public CardAPIController(final CardService cardService) {
		this.cardService = cardService;
	}
	
	/*@PostMapping(value="/issueCard")
	public ResponseEntity<?> issueCardToCustomer(@RequestBody CardDTO dto){
		return cardService.issueCardToCustomer(dto);
	}*/
	@ApiOperation(value = "Card List In SOR", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/list" ,produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<CardView> getCardList(@RequestBody CardDTO dto){
		return cardService.getCardList(dto);
	}

	@ApiOperation(value = "download cards list", authorizations = {@Authorization(value = "basicAuth")})
	@CrossOrigin(origins = {"*"}, allowCredentials = "true", allowedHeaders = "*", methods = RequestMethod.GET)
	@GetMapping(value = "/carddownloadList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void downloadCardList(HttpServletRequest request, HttpServletResponse response,
								 @PathVariable("programUrl") String programUrl,
								 @RequestParam final Map<String, String> requestParams) throws Exception {
		try {
			cardService.downloadCardList(request, response, programUrl, requestParams);
		} catch (BizException e) {
			e.printStackTrace();
			response.sendRedirect(request.getHeader("Referer"));
		}
	}

	
	@ApiOperation(value = "Card Status Update", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/updateStatus",consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateCardStatus(@RequestBody CardDTO dto){
		return cardService.updateCardStatus(dto);
	}

}
