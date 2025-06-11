package com.dataquadinc.dto;


public class Coordinator_DTO {
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private int  getTotalInterviews;
    private int totalSelected;
    private int totalRejected;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public int getGetTotalInterviews() {
        return getTotalInterviews;
    }

    public void setGetTotalInterviews(int getTotalInterviews) {
        this.getTotalInterviews = getTotalInterviews;
    }

    public int getTotalSelected() {
        return totalSelected;
    }

    public void setTotalSelected(int totalSelected) {
        this.totalSelected = totalSelected;
    }

    public int getTotalRejected() {
        return totalRejected;
    }

    public void setTotalRejected(int totalRejected) {
        this.totalRejected = totalRejected;
    }
}
