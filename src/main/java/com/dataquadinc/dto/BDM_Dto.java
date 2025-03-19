package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BDM_Dto {
    private String id;
    private String clientName;
    private String clientAddress;
    private String positionType;
    private int netPayment;
    private double gst;
    private List<String> supportingCustomers;
    private String clientWebsiteUrl;
    private String clientLinkedInUrl;
    private List<String> clientSpocName;
    private List<String> clientSpocEmailid;
    private List<String> supportingDocuments =new ArrayList<>(); ; // Stores file names only
    private List<String> clientSpocLinkedin;
    private List<String> clientSpocMobileNumber;
    private String onBoardedBy;

    @JsonIgnore // Prevents file data from being included in API responses
    private byte[] documentData;
}
