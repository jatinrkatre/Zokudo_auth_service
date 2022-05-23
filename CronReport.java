package com.zokudo.sor.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.zokudo.sor.enums.CronName;
import com.zokudo.sor.enums.Status;

import lombok.Data;

@Data
@Entity
@Table(name="cron_report")
public class CronReport extends AbstractEntity{

	@Column(name="status")
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name="comment")
	private String comment;
	
	@Column(name="cron_name")
	@Enumerated(EnumType.STRING)
	private CronName cronName;
	
	@Column(name="record_count")
	private long recordCount;
	
	@Column(name="execution_time")
	private double executionTime;
}
