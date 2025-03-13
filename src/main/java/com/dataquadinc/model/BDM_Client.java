package com.dataquadinc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BDM_Client {

    @Id
    private String id;  // String-based ID (custom generated)

    private String clientName;
    private String clientAddress;
    private String positionType;
    private int netPayment;
    private double gst;

    @ElementCollection
    private List<String> supportingCustomers;

    private String clientWebsiteUrl;
    private String clientLinkedInUrl;

    @ElementCollection
    private List<String> clientSpocName;

    @ElementCollection
    private List<String> clientSpocEmailid;


    private String supportingDocuments;  // Stores actual file content
    @Lob
    private byte[]  documentedData;


    private List<String> clientSpocLinkedin;
    private List<String> clientSpocMobileNumber;// Stores the uploaded file name

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = "BDM" + UUID.randomUUID().toString().substring(0, 8);  // Generate Unique ID
        }
    }



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

    public byte[] getDocumentedData() {
        return documentedData;
    }

    public void setDocumentedData(byte[] documentedData) {
        this.documentedData = documentedData;
    }

    public List<String> getClientSpocMobileNumber() {
        return clientSpocMobileNumber;
    }

    public void setClientSpocMobileNumber(List<String> clientSpocMobileNumber) {
        this.clientSpocMobileNumber = clientSpocMobileNumber;
    }

    public List<String> getClientSpocLinkedin() {
        return clientSpocLinkedin;
    }

    public void setClientSpocLinkedin(List<String> clientSpocLinkedin) {
        this.clientSpocLinkedin = clientSpocLinkedin;
    }

}
