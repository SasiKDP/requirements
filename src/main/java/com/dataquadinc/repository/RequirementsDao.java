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

    // Get job details by clientName (client_name is assumed to be in RequirementsModel)
    @Query(value = """
     SELECT r.job_id, r.job_title, client.id AS client_id 
     FROM requirements_model r 
     JOIN bdm_client client ON r.client_name = client.client_name 
     WHERE client.client_name = :clientName
 """, nativeQuery = true)
    List<Object[]> findByClientName(@Param("clientName") String clientName);




    // Fetch recruiters for a given jobId
    @Query("SELECT r FROM RequirementsModel r WHERE r.jobId = :jobId")
    Optional<RequirementsModel> findRecruitersByJobId(@Param("jobId") String jobId);

//    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId", nativeQuery = true)
//    List<Object[]> findCandidatesByJobId(@Param("jobId") String jobId);
//
//    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId AND interview_status = 'Scheduled'", nativeQuery = true)
//    List<Object[]> findInterviewScheduledCandidatesByJobId(@Param("jobId") String jobId);

    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId AND user_id = :recruiterId", nativeQuery = true)
    List<Tuple> findCandidatesByJobIdAndRecruiterId(@Param("jobId") String jobId, @Param("recruiterId") String recruiterId);

    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId AND user_id = :recruiterId AND interview_status = 'Scheduled'", nativeQuery = true)
    List<Tuple> findInterviewScheduledCandidatesByJobIdAndRecruiterId(@Param("jobId") String jobId, @Param("recruiterId") String recruiterId);

    @Query(value = "SELECT email, user_name FROM user_details WHERE user_id = :userId AND status != 'inactive'", nativeQuery = true)
    Tuple findUserEmailAndUsernameByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId AND user_id = :recruiterId", nativeQuery = true)
    List<Tuple> findCandidatesByJobIdAndRecruiterIdAndClientName(@Param("jobId") String jobId,
                                                                 @Param("recruiterId") String recruiterId);

    @Query(value = "SELECT * FROM candidates WHERE job_id = :jobId AND user_id = :recruiterId AND interview_status = 'Scheduled' AND client_name = :clientName", nativeQuery = true)
    List<Tuple> findInterviewScheduledCandidatesByJobIdAndRecruiterIdAndClientName(@Param("jobId") String jobId,
                                                                                   @Param("recruiterId") String recruiterId,
                                                                                   @Param("clientName") String clientName);

}

