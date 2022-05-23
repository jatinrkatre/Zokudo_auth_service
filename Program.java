package com.zokudo.sor.entities;

import javax.persistence.*;

import com.zokudo.sor.enums.ProgramPlans;
import lombok.Data;

@Entity
@Table(name="program")
@Data
public class Program extends AbstractEntity {

	@Column(name="program_name")
	private String programName;
	
	@Column(name="program_hash_id")
	private String programHashId;
	
	@Column(name="program_id")
	private long programId;

	@Enumerated(EnumType.STRING)
	@Column(name = "program_plan")
	private ProgramPlans programPlan;
	
	@Column(name="program_type")
	private String programType;
	
}
