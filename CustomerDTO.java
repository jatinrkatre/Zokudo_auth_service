package com.zokudo.sor.dto;

import java.util.Date;

import com.zokudo.sor.enums.ProgramPlans;
import com.zokudo.sor.enums.Status;

import lombok.Data;

@Data
public class CustomerDTO {

	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String customerHashId;
	private String title;
	private String firstName;
	private String lastName;
	private String countryCode;
	private String contactNo;
	private String gender;
	private String dob;
	private String documentType;
	private String documentNo;
	private String documentExpDate;
	private String emailAddress;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String pincode;
	private String country;
	private Status kycType;
	private Status status;
	private String countryOfIssue;
	private String kycRequestId;
	//private Status kycStatus;
	private String kycRefNo;
	private ProgramPlans programType;
	private String programName;
	private String programHashId;
	private int programId;
	private String preferredName;

	/* Filter Request Body*/
	private String dateRange;
	private Date startDate;
	private Date endDate;
	private String page;
	private String size;

   // private String Kyc loadLimit;
    private String balanceLimit;
    private String monthlyLoadLimit;
    private String annualLoadLimit;
    private String accountType;


}
