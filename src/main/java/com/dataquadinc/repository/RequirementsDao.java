package com.dataquadinc.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.dataquadinc.dto.*;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dataquadinc.model.RequirementsModel;

@Repository
public interface RequirementsDao extends JpaRepository<RequirementsModel, String> {

    @Query("SELECT r FROM RequirementsModel r WHERE :recruiterId MEMBER OF r.recruiterIds")
    List<RequirementsModel> findJobsByRecruiterId(String recruiterId);

    @Query("SELECT r FROM RequirementsModel r WHERE r.jobId = :jobId")
    Optional<RequirementsModel> findRecruitersByJobId(@Param("jobId") String jobId);

    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId AND user_id = :recruiterId", nativeQuery = true)
    List<Tuple> findCandidatesByJobIdAndRecruiterId(@Param("jobId") String jobId, @Param("recruiterId") String recruiterId);

    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId AND user_id = :recruiterId AND interview_status = 'Scheduled'", nativeQuery = true)
    List<Tuple> findInterviewScheduledCandidatesByJobIdAndRecruiterId(@Param("jobId") String jobId, @Param("recruiterId") String recruiterId);

    @Query(value = "SELECT email, user_name FROM user_details WHERE user_id = :userId AND status != 'inactive'", nativeQuery = true)
    Tuple findUserEmailAndUsernameByUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT u.user_id, u.user_name, r.name AS role_name, u.email, 
           u.designation, u.joining_date, u.gender, u.dob, 
           u.phone_number, u.personalemail, u.status, b.client_name 
    FROM user_details u 
    LEFT JOIN user_roles ur ON u.user_id = ur.user_id 
    LEFT JOIN roles r ON ur.role_id = r.id
    LEFT JOIN bdm_client b ON u.user_id = b.on_boarded_by
    WHERE r.name = 'BDM' AND u.user_id = :userId
    """, nativeQuery = true)
    List<Tuple> findBdmEmployeeByUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT id, client_name, on_boarded_by, client_address, 
           JSON_UNQUOTE(JSON_EXTRACT(client_spoc_name, '$')) AS client_spoc_name,
           JSON_UNQUOTE(JSON_EXTRACT(client_spoc_emailid, '$')) AS client_spoc_emailid,
           JSON_UNQUOTE(JSON_EXTRACT(client_spoc_mobile_number, '$')) AS client_spoc_mobile_number
    FROM bdm_client 
    WHERE on_boarded_by = (SELECT user_name FROM user_details WHERE user_id = :userId)
    """, nativeQuery = true)
    List<Tuple> findClientsByBdmUserId(@Param("userId") String userId);

    @Query(value = """
        SELECT r.job_id, r.job_title, b.client_name
        FROM requirements_model r
        JOIN bdm_client b ON r.client_name = b.client_name
        WHERE b.on_boarded_by = (SELECT user_name FROM user_details WHERE user_id = :userId)
    """, nativeQuery = true)
    List<Tuple> findJobsByBdmUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT u.user_name AS recruiter_name, 
           r.client_name, 
           r.job_id, 
           r.job_title, 
           r.assigned_by, 
           r.location, 
           r.notice_period
    FROM requirements_model r
    JOIN job_recruiters jr 
        ON r.job_id = jr.job_id
    JOIN user_details u 
        ON jr.recruiter_id = u.user_id
    JOIN bdm_client b 
        ON TRIM(UPPER(r.client_name)) COLLATE utf8mb4_bin = TRIM(UPPER(b.client_name)) COLLATE utf8mb4_bin
    WHERE TRIM(UPPER(b.client_name)) COLLATE utf8mb4_bin = TRIM(UPPER(:clientName)) COLLATE utf8mb4_bin
    AND r.job_id IS NOT NULL
    """, nativeQuery = true)
    List<Tuple> findRequirementsByClientName(@Param("clientName") String clientName);

    @Query(value = """
        SELECT c.candidate_id, c.full_name, c.candidate_email_id AS candidateEmailId, 
               c.contact_number, c.qualification, c.skills, c.overall_feedback, c.user_id,
               r.job_id, r.job_title, b.client_name
        FROM candidates c
        JOIN requirements_model r ON c.job_id = r.job_id
        JOIN bdm_client b ON r.client_name = b.client_name
        WHERE b.client_name = :clientName
        """, nativeQuery = true)
    List<Tuple> findAllSubmissionsByClientName(@Param("clientName") String clientName);

    @Query(value = """
    SELECT c.candidate_id, 
           c.full_name, 
           c.candidate_email_id AS candidateEmailId, 
           c.contact_number, 
           c.qualification, 
           c.skills, 
           CASE 
               WHEN JSON_VALID(c.interview_status) 
               THEN JSON_UNQUOTE(JSON_EXTRACT(c.interview_status, '$[0].status')) 
               ELSE c.interview_status 
           END AS interview_status, 
           c.interview_level, 
           c.interview_date_time, 
           r.job_id, 
           r.job_title, 
           b.client_name
    FROM (
        SELECT candidate_id, full_name, candidate_email_id, contact_number, qualification, 
               skills, interview_status, interview_level, interview_date_time, job_id, 
               ROW_NUMBER() OVER (PARTITION BY candidate_id ORDER BY interview_date_time DESC) AS rn 
        FROM candidates 
    ) c
    JOIN requirements_model r ON c.job_id = r.job_id
    LEFT JOIN bdm_client b ON r.client_name = b.client_name
    WHERE (b.client_name = :clientName OR r.client_name = :clientName 
           OR (:clientName IS NULL AND EXISTS (
                SELECT 1 FROM candidates c2 
                WHERE c2.job_id = r.job_id
           )) )
    AND c.rn = 1
    AND b.client_name IS NOT NULL 
    AND c.interview_date_time IS NOT NULL
    """, nativeQuery = true)
    List<Tuple> findAllInterviewsByClientName(@Param("clientName") String clientName);

    @Query(value = """
    SELECT c.candidate_id, 
           c.full_name, 
           c.candidate_email_id AS candidateEmailId,  
           r.job_id, 
           r.job_title, 
           b.client_name
    FROM candidates c
    JOIN requirements_model r ON c.job_id = r.job_id
    JOIN bdm_client b ON r.client_name = b.client_name
    WHERE b.client_name = :clientName
    AND (
        (JSON_VALID(c.interview_status) 
         AND JSON_SEARCH(c.interview_status, 'one', 'Placed', NULL, '$[*].status') IS NOT NULL)
        OR UPPER(c.interview_status) = 'PLACED'
    )
    """, nativeQuery = true)
    List<Tuple> findAllPlacementsByClientName(@Param("clientName") String clientName);

    @Query(value = """
    SELECT 
        u.user_id AS employeeId,
        u.user_name AS employeeName,
        u.email AS employeeEmail,
        r.name AS role,
        COALESCE((SELECT COUNT(DISTINCT c.candidate_id) 
                  FROM candidates c 
                  WHERE c.user_id = u.user_id), 0) AS numberOfSubmissions,
        COALESCE(
                        (SELECT SUM(CASE
                            WHEN c.interview_status = 'Scheduled'
                                 OR c.interview_date_time IS NOT NULL
                                 AND r.client_name IS NOT NULL
                                 AND c.job_id = r.job_id
                            THEN 1 ELSE 0
                        END)
                        FROM candidates c
                        JOIN requirements_model r ON c.job_id = r.job_id
                        JOIN bdm_client bc ON r.client_name = bc.client_name
                        WHERE c.user_id = u.user_id),
                    0) AS numberOfInterviews,
        COALESCE((SELECT SUM(CASE 
                            WHEN c.interview_status = 'Placed' THEN 1 ELSE 0 
                        END) 
                  FROM candidates c 
                  WHERE c.user_id = u.user_id), 0) +
        COALESCE((SELECT SUM(CASE 
                            WHEN JSON_VALID(c.interview_status) = 1  
                            AND JSON_SEARCH(c.interview_status, 'one', 'Placed', NULL, '$[*].status') IS NOT NULL 
                            THEN 1 ELSE 0 
                        END) 
                  FROM candidates c 
                  WHERE c.user_id = u.user_id), 0) AS numberOfPlacements,
        COALESCE((SELECT COUNT(DISTINCT req.client_name) 
                  FROM requirements_model req 
                  JOIN job_recruiters jrp ON req.job_id = jrp.job_id
                  WHERE jrp.recruiter_id = u.user_id), 0) AS numberOfClients,
        COALESCE((SELECT COUNT(DISTINCT req.job_id) 
                  FROM requirements_model req 
                  JOIN job_recruiters jrp ON req.job_id = jrp.job_id
                  WHERE jrp.recruiter_id = u.user_id), 0) AS numberOfRequirements
    FROM user_details u
    JOIN user_roles ur ON u.user_id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.name IN ('Employee', 'Teamlead')
    """, nativeQuery = true)
    List<Tuple> getEmployeeCandidateStats();

    @Query(value = """
    SELECT 
        c.candidate_id AS candidateId,
        c.full_name AS fullName,
        c.candidate_email_id AS candidateEmailId,
        c.contact_number AS contactNumber,
        c.qualification AS qualification,
        c.skills AS skills,
        c.overall_feedback AS overallFeedback,
        r.job_id AS jobId,
        r.job_title AS jobTitle,
        r.client_name AS clientName
    FROM candidates c
    JOIN user_details u ON c.user_id = u.user_id
    JOIN requirements_model r ON c.job_id = r.job_id
    WHERE u.user_id = :userId
    """, nativeQuery = true)
    List<SubmittedCandidateDTO> findSubmittedCandidatesByUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT 
        c.candidate_id AS candidateId,
        c.full_name AS fullName,
        c.candidate_email_id AS candidateEmailId,
        c.contact_number AS contactNumber,
        c.qualification AS qualification,
        c.skills AS skills,
        CASE 
            WHEN JSON_VALID(c.interview_status) = 1 
            THEN JSON_UNQUOTE(JSON_EXTRACT(c.interview_status, '$[0].status')) 
            ELSE c.interview_status 
        END AS interviewStatus,
        c.interview_level AS interviewLevel,
        c.interview_date_time AS interviewDateTime,
        r.job_id AS jobId,
        r.job_title AS jobTitle,
        r.client_name AS clientName
    FROM candidates c
    JOIN user_details u ON c.user_id = u.user_id
    JOIN requirements_model r ON c.job_id = r.job_id
    WHERE u.user_id = :userId
      AND c.interview_date_time IS NOT NULL
      AND c.client_name IS NOT NULL
    """, nativeQuery = true)
    List<InterviewScheduledDTO> findScheduledInterviewsByUserId(@Param("userId") String userId);

    @Query(value = """
        SELECT 
            r.job_id AS jobId,
            TRIM(r.job_title) AS jobTitle,
            TRIM(r.client_name) AS clientName,
            TRIM(BOTH '\\"' FROM r.assigned_by) AS assignedBy,
            r.status AS status,
            r.no_of_positions AS noOfPositions,
            r.qualification AS qualification,
            r.job_type AS jobType,
            r.job_mode AS jobMode,
            r.requirement_added_time_stamp AS postedDate
        FROM requirements_model r
        JOIN job_recruiters jr ON r.job_id = jr.job_id
        JOIN user_details u ON jr.recruiter_id = u.user_id
        WHERE u.user_id = :userId
    """, nativeQuery = true)
    List<JobDetailsDTO> findJobDetailsByUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT 
        c.candidate_id AS candidateId,
        c.full_name AS fullName,
        c.candidate_email_id AS candidateEmailId,
        c.contact_number AS contactNumber,
        c.qualification AS qualification,
        c.skills AS skills,
        CASE 
            WHEN JSON_VALID(c.interview_status) 
            THEN JSON_UNQUOTE(JSON_EXTRACT(c.interview_status, '$[0].status')) 
            ELSE c.interview_status 
        END AS interviewStatus,
        c.interview_level AS interviewLevel,
        c.interview_date_time AS interviewDateTime,
        r.job_id AS jobId,
        r.job_title AS jobTitle,
        r.client_name AS clientName
    FROM candidates c
    JOIN user_details u ON c.user_id = u.user_id
    JOIN requirements_model r ON c.job_id = r.job_id
    WHERE u.user_id = :userId
      AND (
          (JSON_VALID(c.interview_status) AND JSON_UNQUOTE(JSON_EXTRACT(c.interview_status, '$[0].status')) = 'Placed')
          OR c.interview_status = 'Placed'
      )
    """, nativeQuery = true)
    List<PlacementDetailsDTO> findPlacementCandidatesByUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT 
        b.id AS clientId,  
        b.client_name AS clientName,  
        b.client_address AS clientAddress,  
        b.on_boarded_by AS onBoardedBy,  
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(JSON_UNQUOTE(JSON_EXTRACT(b.client_spoc_name, '$')), '[\"', ''), '\"]', ''), '\\\\"', ''), '\\\\', ''), '"', '') AS clientSpocName,  
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(JSON_UNQUOTE(JSON_EXTRACT(b.client_spoc_mobile_number, '$')), '[\"', ''), '\"]', ''), '\\\\"', ''), '\\\\', ''), '"', '') AS clientSpocMobileNumber
    FROM bdm_client b
    JOIN requirements_model r ON LOWER(b.client_name) = LOWER(r.client_name)  
    JOIN job_recruiters jr ON r.job_id = jr.job_id  
    JOIN user_details u ON jr.recruiter_id = u.user_id  
    WHERE u.user_id = :userId
    """, nativeQuery = true)
    List<ClientDetailsDTO> findClientDetailsByUserId(@Param("userId") String userId);

    @Query(value = """
        SELECT 
            u.user_id AS employeeId,
            u.user_name AS employeeName,                
            r.name AS role,
            u.email AS employeeEmail,
            u.designation AS designation,
            DATE_FORMAT(u.joining_date, '%Y-%m-%d') AS joiningDate, 
            u.gender AS gender, 
            DATE_FORMAT(u.dob, '%Y-%m-%d') AS dob, 
            u.phone_number AS phoneNumber, 
            u.personalemail AS personalEmail, 
            u.status AS status
        FROM user_details u
        JOIN user_roles ur ON u.user_id = ur.user_id
        JOIN roles r ON ur.role_id = r.id
        WHERE r.name IN ('Employee', 'Teamlead')
        AND u.user_id = :userId
    """, nativeQuery = true)
    List<Tuple> getEmployeeDetailsByUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT COUNT(DISTINCT c.candidate_id)
    FROM candidates c
    JOIN requirements_model req ON c.job_id = req.job_id
    WHERE c.job_id = :jobId
""", nativeQuery = true)
    Integer getNumberOfSubmissionsByJobId(@Param("jobId") String jobId);


    @Query(value = """
    SELECT COALESCE(SUM(CASE 
                        WHEN c.interview_date_time IS NOT NULL
                             AND req.job_id = :jobId
                        THEN 1 ELSE 0 
                        END), 0)
    FROM candidates c
    JOIN requirements_model req ON c.job_id = req.job_id
    WHERE req.job_id = :jobId
""", nativeQuery = true)
    Integer getNumberOfInterviewsByJobId(@Param("jobId") String jobId);

    // RequirementsDao.java
    @Query("SELECT r FROM RequirementsModel r WHERE DATE(r.requirementAddedTimeStamp) BETWEEN :startDate AND :endDate")
    List<RequirementsModel> findByRequirementAddedTimeStampBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<RequirementsModel> findByAssignedByIgnoreCase(String assignedBy);
}
