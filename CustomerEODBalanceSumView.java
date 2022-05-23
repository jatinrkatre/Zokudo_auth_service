package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Data
@Immutable
@Entity
@Table(name="customer_balance_eod_sum_view")
public class CustomerEODBalanceSumView {
	
	@Id
	@Column(name="id")
	private long id;

	@Column(name="last_updated_at")
	private Date lastUpdatedAt;
	
	/*@Column(name="balance")
	private Double balance;*/

	@Column(name="gpr_balance")
	private Double gprBalance;

	@Column(name="gc_balance")
	private Double gcBalance;
	
	@Column(name="customer_name")
	private String customerName;
	
	@Column(name="mobile")
	private String mobile;
	
}
