package com.zokudo.sor.service;

import org.springframework.http.ResponseEntity;

import com.zokudo.sor.dto.ProgramDTO;

public interface ProgramService {

	public ResponseEntity<?> addProgram(ProgramDTO dto);
}
