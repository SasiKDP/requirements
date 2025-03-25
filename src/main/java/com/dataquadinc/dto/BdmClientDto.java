
package com.dataquadinc.dto;

import java.util.List;

public class BdmClientDto {
    private String clientId;
    private String clientName;
    private String onBoardedBy;
    private String clientAddress;
    private List<String> clientSpocName;
    private List<String> clientSpocEmailid;
    private List<String> clientSpocMobileNumber;

    public BdmClientDto(String clientId, String clientName, String onBoardedBy, String clientAddress,
                        List<String> clientSpocName, List<String> clientSpocEmailid,
                        List<String> clientSpocMobileNumber) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.onBoardedBy = onBoardedBy;
        this.clientAddress = clientAddress;
        this.clientSpocName = clientSpocName;
        this.clientSpocEmailid = clientSpocEmailid;
        this.clientSpocMobileNumber = clientSpocMobileNumber;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getOnBoardedBy() {
        return onBoardedBy;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public List<String> getClientSpocName() {
        return clientSpocName;
    }

    public List<String> getClientSpocEmailid() {
        return clientSpocEmailid;
    }

    public List<String> getClientSpocMobileNumber() {
        return clientSpocMobileNumber;
    }
}
