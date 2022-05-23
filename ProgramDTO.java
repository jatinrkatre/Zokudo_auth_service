package com.zokudo.sor.dto;

import com.zokudo.sor.enums.ProgramPlans;


import lombok.Data;

@Data
public class ProgramDTO {

	private String programName;
	private String programHashId;
	private long programId;
	private String createdAt;
	private String updatedAt;
	private ProgramPlans programPlan;
	private String programType;
	
}
