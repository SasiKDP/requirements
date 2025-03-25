package com.dataquadinc.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dataquadinc.model.RequirementsModel;

@Repository
public interface RequirementsDao extends JpaRepository<RequirementsModel, String>
{
    @Query("SELECT r FROM RequirementsModel r WHERE :recruiterId MEMBER OF r.recruiterIds")
    List<RequirementsModel> findJobsByRecruiterId(String recruiterId);
    // Fetch recruiters for a given jobId
    @Query("SELECT r FROM RequirementsModel r WHERE r.jobId = :jobId")
    Optional<RequirementsModel> findRecruitersByJobId(@Param("jobId") String jobId);

    @Query(value = "SELECT * FROM candidates_prod WHERE job_id = :jobId AND user_id = :recruiterId", nativeQuery = true)
    List<Tuple> findCandidatesByJobIdAndRecruiterId(@Param("jobId") String jobId, @Param("recruiterId") String recruiterId);

    @Query(value = "SELECT * FROM candidates_prod WHERE job_id = :jobId AND user_id = :recruiterId AND interview_status = 'Scheduled'", nativeQuery = true)
    List<Tuple> findInterviewScheduledCandidatesByJobIdAndRecruiterId(@Param("jobId") String jobId, @Param("recruiterId") String recruiterId);

    @Query(value = "SELECT email, user_name FROM user_details_prod WHERE user_id = :userId AND status != 'inactive'", nativeQuery = true)
    Tuple findUserEmailAndUsernameByUserId(@Param("userId") String userId);


    @Query(value = """
    SELECT u.user_id, u.user_name, r.name AS role_name, u.email, 
           u.designation, u.joining_date, u.gender, u.dob, 
           u.phone_number, u.personalemail, u.status, b.client_name 
    FROM user_details_prod u 
    LEFT JOIN user_roles_prod ur ON u.user_id = ur.user_id 
    LEFT JOIN roles_prod r ON ur.role_id = r.id
    LEFT JOIN bdm_client_prod b ON u.user_id = b.on_boarded_by
    WHERE r.name = 'BDM' AND u.user_id = :userId
    """, nativeQuery = true)
    List<Tuple> findBdmEmployeeByUserId(@Param("userId") String userId);


    // Get Clients onboarded by BDM (based on userId)
    @Query(value = """
    SELECT id, client_name, on_boarded_by, client_address, 
           JSON_UNQUOTE(JSON_EXTRACT(client_spoc_name, '$')) AS client_spoc_name,
           JSON_UNQUOTE(JSON_EXTRACT(client_spoc_emailid, '$')) AS client_spoc_emailid,
           JSON_UNQUOTE(JSON_EXTRACT(client_spoc_mobile_number, '$')) AS client_spoc_mobile_number
    FROM bdm_client_prod 
    WHERE on_boarded_by = (SELECT user_name FROM user_details_prod WHERE user_id = :userId)
""", nativeQuery = true)
    List<Tuple> findClientsByBdmUserId(@Param("userId") String userId);

    // Get all job IDs and client names onboarded by BDM
    @Query(value = """
        SELECT r.job_id, r.job_title, b.client_name
        FROM requirements_model_prod r
        JOIN bdm_client_prod b ON r.client_name = b.client_name
        WHERE b.on_boarded_by = (SELECT user_name FROM user_details_prod WHERE user_id = :userId)
    """, nativeQuery = true)
    List<Tuple> findJobsByBdmUserId(@Param("userId") String userId);

    // Fetch all submissions for a client across ALL job IDs
    @Query(value = """
        SELECT c.candidate_id, c.full_name, c.candidate_email_id AS candidateEmailId, 
               c.contact_number, c.qualification, c.skills, c.overall_feedback, c.user_id,
               r.job_id, r.job_title, b.client_name
        FROM candidates_prod c
        JOIN requirements_model_prod r ON c.job_id = r.job_id
        JOIN bdm_client_prod b ON r.client_name = b.client_name
        WHERE b.client_name = :clientName
        """, nativeQuery = true)
    List<Tuple> findAllSubmissionsByClientName(@Param("clientName") String clientName);

    // Fetch all interview scheduled candidates for a client
    @Query(value = """
    SELECT c.candidate_id, 
           c.full_name, 
           c.candidate_email_id AS candidateEmailId, 
           c.contact_number, 
           c.qualification, 
           c.skills, 
           -- ✅ Extract the latest status from JSON or direct string
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
        FROM candidates_prod 
    ) c
    JOIN requirements_model_prod r ON c.job_id = r.job_id
    LEFT JOIN bdm_client_prod b ON r.client_name = b.client_name
    WHERE (b.client_name = :clientName OR r.client_name = :clientName 
           OR (:clientName IS NULL AND EXISTS (
                SELECT 1 FROM candidates_prod c2 
                WHERE c2.job_id = r.job_id
           )) )
    AND c.rn = 1  -- ✅ Fetch only the latest interview status per candidate
    -- ✅ Ensure only "Scheduled" candidates are included
    AND b.client_name IS NOT NULL 
    AND c.interview_date_time IS NOT NULL
""", nativeQuery = true)
    List<Tuple> findAllInterviewsByClientName(@Param("clientName") String clientName);




    // Fetch all placements for a client across ALL job IDs
    @Query(value = """
    SELECT c.candidate_id, 
           c.full_name, 
           c.candidate_email_id AS candidateEmailId,  
           r.job_id, 
           r.job_title, 
           b.client_name
    FROM candidates_prod c
    JOIN requirements_model_prod r ON c.job_id = r.job_id
    JOIN bdm_client_prod b ON r.client_name = b.client_name
    WHERE b.client_name = :clientName
    AND (
        -- ✅ Check if interview_status is a valid JSON and contains "Placed"
        (JSON_VALID(c.interview_status) 
         AND JSON_SEARCH(c.interview_status, 'one', 'Placed', NULL, '$[*].status') IS NOT NULL)
        -- ✅ OR check if interview_status is stored as plain text "Placed"
        OR UPPER(c.interview_status) = 'PLACED'
    )
""", nativeQuery = true)
    List<Tuple> findAllPlacementsByClientName(@Param("clientName") String clientName);


}
