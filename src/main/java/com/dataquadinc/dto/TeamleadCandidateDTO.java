package com.dataquadinc.dto;

public class TeamleadCandidateDTO {

    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private String role;

    private int numberOfClients;
    private int numberOfRequirements;

    private int selfSubmissions;
    private int selfInterviews;
    private int selfPlacements;

    private int teamSubmissions;
    private int teamInterviews;
    private int teamPlacements;

    // Constructors
    public TeamleadCandidateDTO() {}

    public TeamleadCandidateDTO(String employeeId, String employeeName, String employeeEmail, String role,
                                int numberOfClients, int numberOfRequirements,
                                int selfSubmissions, int selfInterviews, int selfPlacements,
                                int teamSubmissions, int teamInterviews, int teamPlacements) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.role = role;
        this.numberOfClients = numberOfClients;
        this.numberOfRequirements = numberOfRequirements;
        this.selfSubmissions = selfSubmissions;
        this.selfInterviews = selfInterviews;
        this.selfPlacements = selfPlacements;
        this.teamSubmissions = teamSubmissions;
        this.teamInterviews = teamInterviews;
        this.teamPlacements = teamPlacements;
    }

    // Getters and Setters

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

    public int getSelfSubmissions() {
        return selfSubmissions;
    }

    public void setSelfSubmissions(int selfSubmissions) {
        this.selfSubmissions = selfSubmissions;
    }

    public int getSelfInterviews() {
        return selfInterviews;
    }

    public void setSelfInterviews(int selfInterviews) {
        this.selfInterviews = selfInterviews;
    }

    public int getSelfPlacements() {
        return selfPlacements;
    }

    public void setSelfPlacements(int selfPlacements) {
        this.selfPlacements = selfPlacements;
    }

    public int getTeamSubmissions() {
        return teamSubmissions;
    }

    public void setTeamSubmissions(int teamSubmissions) {
        this.teamSubmissions = teamSubmissions;
    }

    public int getTeamInterviews() {
        return teamInterviews;
    }

    public void setTeamInterviews(int teamInterviews) {
        this.teamInterviews = teamInterviews;
    }

    public int getTeamPlacements() {
        return teamPlacements;
    }

    public void setTeamPlacements(int teamPlacements) {
        this.teamPlacements = teamPlacements;
    }
}
