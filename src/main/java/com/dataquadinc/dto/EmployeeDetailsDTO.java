package com.dataquadinc.dto;

import java.time.LocalDate;

public class EmployeeDetailsDTO {
    private String employeeId;
    private String employeeName;
    private String role;
    private String employeeEmail;
    private String designation;
    private LocalDate joiningDate; // Changed to LocalDate
    private String gender;
    private LocalDate dob;         // Changed to LocalDate
    private String phoneNumber;
    private String personalEmail;
    private String status;

    // Constructor to map query results
    public EmployeeDetailsDTO(String employeeId, String employeeName, String role, String employeeEmail,
                              String designation, LocalDate joiningDate, String gender, LocalDate dob,
                              String phoneNumber, String personalEmail, String status) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.role = role;
        this.employeeEmail = employeeEmail;
        this.designation = designation;
        this.joiningDate = joiningDate;
        this.gender = gender;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
        this.personalEmail = personalEmail;
        this.status = status;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
