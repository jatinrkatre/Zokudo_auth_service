package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Immutable
@Table(name="customer_view")
@Data
public class CustomerView {

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="created_at")
	private Date createdAt;
	
	@Column(name="mobile")
	private String mobile;
	
	@Column(name="customer_hash_id")
	private String customerHashId;
	
	@Column(name="kyc_type")
	private String kycType;
	
	@Column(name="customer_name")
	private String customerName;
	
	@Column(name="id_type")
	private String idType;
	
	@Column(name="id_number")
	private String idNumber;
	
	@Column(name="id_expiry")
	private String idExpiryDate;
	
	@Column(name="program_name")
	private String programName;
	
	@Column(name="program_hash_id")
	private String programHashId;

	@Column(name="email")
	private String emailAddress;
	
	@Column(name="programType")
	private String programType;

	@Column(name="original_created_at")
	private Date originalCreatedAt;
}
