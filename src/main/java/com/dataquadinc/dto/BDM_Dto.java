package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class BDM_Dto {
    private String id;  // Changed from String to Long
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
    private String supportingDocuments; // Store only the file name
    private String paymentType;
    private List<String> clientSpocLinkedin;
    private List<String> clientSpocMobileNumber;


    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    @JsonIgnore // Prevent file data from being returned in API response
    private byte[] documentData;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public int getNetPayment() {
        return netPayment;
    }

    public void setNetPayment(int netPayment) {
        this.netPayment = netPayment;
    }

    public double getGst() {
        return gst;
    }

    public void setGst(double gst) {
        this.gst = gst;
    }

    public List<String> getSupportingCustomers() {
        return supportingCustomers;
    }

    public void setSupportingCustomers(List<String> supportingCustomers) {
        this.supportingCustomers = supportingCustomers;
    }

    public String getClientWebsiteUrl() {
        return clientWebsiteUrl;
    }

    public void setClientWebsiteUrl(String clientWebsiteUrl) {
        this.clientWebsiteUrl = clientWebsiteUrl;
    }

    public String getClientLinkedInUrl() {
        return clientLinkedInUrl;
    }

    public void setClientLinkedInUrl(String clientLinkedInUrl) {
        this.clientLinkedInUrl = clientLinkedInUrl;
    }

    public List<String> getClientSpocName() {
        return clientSpocName;
    }

    public void setClientSpocName(List<String> clientSpocName) {
        this.clientSpocName = clientSpocName;
    }

    public List<String> getClientSpocEmailid() {
        return clientSpocEmailid;
    }

    public void setClientSpocEmailid(List<String> clientSpocEmailid) {
        this.clientSpocEmailid = clientSpocEmailid;
    }

    public String getSupportingDocuments() {
        return supportingDocuments;
    }

    public void setSupportingDocuments(String supportingDocuments) {
        this.supportingDocuments = supportingDocuments;
    }

    public List<String> getClientSpocLinkedin() {
        return clientSpocLinkedin;
    }

    public void setClientSpocLinkedin(List<String> clientSpocLinkedin) {
        this.clientSpocLinkedin = clientSpocLinkedin;
    }

    public List<String> getClientSpocMobileNumber() {
        return clientSpocMobileNumber;
    }

    public void setClientSpocMobileNumber(List<String> clientSpocMobileNumber) {
        this.clientSpocMobileNumber = clientSpocMobileNumber;
    }

    public byte[] getDocumentData() {
        return documentData;
    }

    public void setDocumentData(byte[] documentData) {
        this.documentData = documentData;
    }
}
