package com.zokudo.sor.dto;

import lombok.Data;

@Data
public class CustomerKycLimitBasedOnFiltersDTO {

    private String page;
    private String size;
    private String dateRange;
    private String mobileNumber;
}
