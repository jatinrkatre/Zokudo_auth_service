package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="customer_eod_total_balance")
public class CustomerEODSumBalance extends AbstractEntity{

	@Column(name="customer_mobile")
	private String customerMobile;
	
	/*@Column(name="balance")
	private Double balance;*/

	@Column(name="gpr_balance")
	private Double gprBalance;

	@Column(name="gc_balance")
	private Double gcBalance;
	
	@Column(name="eod_created_at")
	private Date eodCreatedAt;
}
