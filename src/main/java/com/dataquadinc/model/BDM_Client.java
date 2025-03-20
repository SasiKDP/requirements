package com.dataquadinc.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "BDM_Client_prod")
public class BDM_Client {

    @Id
    private String id;  // Custom-generated ID

    private String clientName;
    private String onBoardedBy;
    private String clientAddress;
    private int netPayment;
    private double gst;

    @ElementCollection
    @CollectionTable(name = "client_supporting_customers", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "customer_name")
    private List<String> supportingCustomers;
    @Column(length = 2000)
    @Pattern(regexp = "^(https?:\\/\\/)?([\\w.-]+)+(:\\d+)?(\\/.*)?$",
            message = "Invalid LinkedIn URL format")
    private String clientWebsiteUrl;
    @Column(length = 2000)
    @Pattern(regexp = "^(https?:\\/\\/)?([\\w.-]+)+(:\\d+)?(\\/.*)?$",
            message = "Invalid LinkedIn URL format")
    private String clientLinkedInUrl;

    @ElementCollection
    @CollectionTable(name = "client_spoc_names", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "spoc_name")
    private List<String> clientSpocName;

    @ElementCollection
    @CollectionTable(name = "client_spoc_emails", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "spoc_email")
    private List<String> clientSpocEmailid;

    @ElementCollection
    @CollectionTable(name = "client_spoc_linkedin", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "spoc_linkedin")
    private List<String> clientSpocLinkedin;

    @ElementCollection
    @CollectionTable(name = "client_spoc_mobile_numbers", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "spoc_mobile_number")
    private List<String> clientSpocMobileNumber;

    @ElementCollection
    @CollectionTable(name = "client_supporting_documents", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "document_name")
    private List<String> supportingDocuments =new ArrayList<>();  // Stores file paths as JSON

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] documentedData;  // Stores actual file content

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = "BDM" + UUID.randomUUID().toString().substring(0, 8);  // Generate Unique ID
        }
    }


}