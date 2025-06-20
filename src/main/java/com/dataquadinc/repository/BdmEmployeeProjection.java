package com.dataquadinc.repository;

public interface BdmEmployeeProjection {
    String getUserId();
    String getUserName();
    String getEmail();
    String getStatus();
    String getRoleName();  // If you're manually joining to roles
}
