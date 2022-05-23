package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="customer_eod_balance")
public class CustomerEODBalance extends AbstractEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name="customer_hash_id")
	private String customerHashId;
	
	@Column(name="balance")
	private Double balance;
	
	@Column(name="eod_created_at")
	private Date eodCreatedAt;
	
}
