package com.dataquadinc.model;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "BDM_Client")
public class BDM_Client {

    @Id
    private String id;  // Custom-generated ID
    @Column(unique = true, nullable = false)
    private String clientName;
    private String onBoardedBy;
    private String clientAddress;
    private String positionType;
    private int netPayment;
    private double gst;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> supportingCustomers;

    private String clientWebsiteUrl;
    @Column(length = 1000)
    private String clientLinkedInUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> clientSpocName;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> clientSpocEmailid;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> clientSpocLinkedin;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> clientSpocMobileNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> supportingDocuments;  // Stores file paths as JSON

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] documentedData;  // Stores actual file content

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = "BDM" + UUID.randomUUID().toString().substring(0, 8);  // Generate Unique ID
        }
    }
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}