package com.zokudo.sor.service.impl;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.zokudo.sor.dto.ProgramDTO;
import com.zokudo.sor.entities.Program;
import com.zokudo.sor.exceptions.BizException;
import com.zokudo.sor.repositories.ProgramRepository;
import com.zokudo.sor.service.ProgramService;
import com.zokudo.sor.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProgramServiceImpl implements ProgramService {

	private final ProgramRepository programRepository;
	
	@Autowired
	public ProgramServiceImpl(final ProgramRepository programRepository) {
		this.programRepository = programRepository;
	}
	
	@Override
	public ResponseEntity<?> addProgram(ProgramDTO dto) {
		try {
			log.info("** Adding Program at SOR END ");
			insertProgram(dto);
			return new ResponseEntity<>(" Program Created. ",HttpStatus.OK);
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new BizException("Error while saving program to DB. "+e.getMessage());
		}
		
	}

	private void insertProgram(ProgramDTO dto) throws ParseException {
		
		log.info("** Inserting program: programName {} | programHashId {} | programId {} | created At {} | updatedAt {} ",dto.getProgramName(),dto.getProgramHashId(),dto.getProgramId()
				,dto.getCreatedAt(),dto.getUpdatedAt()); 
		Program program = new Program();
		program.setProgramHashId(dto.getProgramHashId());
		program.setProgramId(dto.getProgramId());
		program.setProgramName(dto.getProgramName());
		program.setProgramPlan(dto.getProgramPlan());
		program.setProgramType(dto.getProgramType());
		if(StringUtils.isNotBlank(dto.getCreatedAt())) {
			Date createdAt = CommonUtil.dateFormate.parse(dto.getCreatedAt());
			program.setCreatedAt(createdAt);
		}else {
			log.error("**  Created At {} ",dto.getCreatedAt());
			throw new BizException("Created At cannot be empty ");
		}
		if(StringUtils.isNotBlank(dto.getUpdatedAt())) {
			Date updatedAt = CommonUtil.dateFormate.parse(dto.getUpdatedAt());
			program.setUpdatedAt(updatedAt);
		}else {
			log.error("**  Updated At {} ",dto.getCreatedAt());
			throw new BizException("Updated At cannot be empty ");
		}
		program.setCreatedBy("mss-admin");
		program.setUpdatedBy("mss-admin");
		
		program = programRepository.save(program);
		log.info("** Added Program succesfully. Program HashID {} ",program.getProgramHashId());
	}

}
