package com.zokudo.sor.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.zokudo.sor.enums.CronName;
import com.zokudo.sor.enums.Status;

import lombok.Data;

@Table(name="cron_failure")
@Data
@Entity
public class CronFailure extends AbstractEntity{
	
	@Column(name="cron_name", unique =true)
	@Enumerated(EnumType.STRING)
	private CronName cronName;

	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	private Status status;
	
	
	@Column(name="failure_date")
	private Date failureDate;
	
	@Column(name="fail_reason")
	private String failReason;
}
