package com.zokudo.sor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zokudo.sor.dto.ProgramDTO;
import com.zokudo.sor.service.ProgramService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@RestController
@RequestMapping("{programUrl}/api/v1/program")
public class ProgramAPIController {
	
	private final ProgramService programService;
	@Autowired
	public ProgramAPIController(final ProgramService programService) {
		this.programService = programService;
	}
	
	@ApiOperation(value = "Add new program", authorizations = {@Authorization("basicAuth")})
    @CrossOrigin(allowedHeaders = "*", allowCredentials = "true", origins = {"*"})
	@PostMapping(value="/add" ,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addProgram(@RequestBody ProgramDTO dto){
		return programService.addProgram(dto);
	}

}
