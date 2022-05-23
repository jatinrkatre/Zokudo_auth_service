package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import com.zokudo.sor.enums.ProgramPlans;

import lombok.Data;

@Data
@Immutable
@Entity
@Table(name="customer_balance_eod_view")
public class CustomerEODBalanceView {
	
	@Id
	@Column(name="id")
	private long id;

	@Column(name="last_updated_at")
	private Date lastUpdatedAt;
	
	@Column(name="customer_hash_id")
	private String customerHashId;
	
	@Column(name="balance")
	private Double balance;
	
	@Column(name="customer_name")
	private String customerName;
	
	@Column(name="mobile")
	private String mobile;
	
	@Column(name="program_name")
	private String programName;
	
	@Column(name="program_plan")
	@Enumerated(EnumType.STRING)
	private ProgramPlans programPlan;
	
	@Column(name="program_type")
	private String programType;
	
}
