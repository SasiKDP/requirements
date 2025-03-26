package com.dataquadinc.dto;

public class EmployeeCandidateDTO {
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private String role;
    private int numberOfClients;  // New field for number of clients
    private int numberOfRequirements;
    private int numberOfSubmissions;
    private int numberOfInterviews;
    private int numberOfPlacements;
      // New field for number of requirements

    // Updated constructor to include new fields
    public EmployeeCandidateDTO(String employeeId, String employeeName, String employeeEmail,
                                String role, int numberOfClients, int numberOfRequirements, int numberOfSubmissions, int numberOfInterviews,
                                int numberOfPlacements) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.role = role;
        this.numberOfClients = numberOfClients;  // Initialize the number of clients
        this.numberOfRequirements = numberOfRequirements;  // Initialize the number of requirements
        this.numberOfSubmissions = numberOfSubmissions;
        this.numberOfInterviews = numberOfInterviews;
        this.numberOfPlacements = numberOfPlacements;
    }

    // Getters and setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getNumberOfSubmissions() {
        return numberOfSubmissions;
    }

    public void setNumberOfSubmissions(int numberOfSubmissions) {
        this.numberOfSubmissions = numberOfSubmissions;
    }

    public int getNumberOfInterviews() {
        return numberOfInterviews;
    }

    public void setNumberOfInterviews(int numberOfInterviews) {
        this.numberOfInterviews = numberOfInterviews;
    }

    public int getNumberOfPlacements() {
        return numberOfPlacements;
    }

    public void setNumberOfPlacements(int numberOfPlacements) {
        this.numberOfPlacements = numberOfPlacements;
    }

    // Getters and setters for the new fields
    public int getNumberOfClients() {
        return numberOfClients;
    }

    public void setNumberOfClients(int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    public int getNumberOfRequirements() {
        return numberOfRequirements;
    }

    public void setNumberOfRequirements(int numberOfRequirements) {
        this.numberOfRequirements = numberOfRequirements;
    }
}
