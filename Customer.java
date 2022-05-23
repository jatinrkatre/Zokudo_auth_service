package com.zokudo.sor.entities;



import javax.persistence.*;
import com.zokudo.sor.enums.Status;

import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "customer", indexes = {
        @Index(name = "account_type_id", columnList = "account_type_id")
})
public class Customer extends AbstractEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "preferred_name")
    private String preferredName;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "customer_hash_id",unique=true)
    private String customerHashId;

    @Column(name = "processor_id")
    private int processorId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_type")
    private Status kycType;

    @Column(name = "agent_hash_id")
    private String agentHashId;

    @Column(name = "distributor_hash_id")
    private String distributorHashId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "user_hash_id")
    private String userHashId;
    
    @Column(name = "title")
    private String title;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birthday")
  //  @Temporal(TemporalType.DATE)
    private String birthday;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "country_of_issue")
    private String countryOfIssue;

    @Column(name = "id_expiry")
    private String idExpiryDate;

    @Column(name="address_1")
    private String address1;

    @Column(name="address_2")
    private String address2;

    @Column(name="city")
    private String city;

    @Column(name="state")
    private String state;

    @Column(name="country")
    private String country;

    @Column(name="zip_code" )
    private String zipCode;

    @Column(name = "kyc_request_id")
    private String kycRequestId;

    @Column(name = "program_id")
    private int programId;
    
    @Column(name="customer_id")
    private long customerId;

    @Column(name="account_type_id")
    private long accountTypeId;

    @Column(name="original_created_at")
    private Date originalCreatedAt ;


}
