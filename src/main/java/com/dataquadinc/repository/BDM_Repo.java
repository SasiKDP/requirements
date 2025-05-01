package com.dataquadinc.repository;



import com.dataquadinc.model.BDM_Client;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BDM_Repo extends JpaRepository<BDM_Client,String> {
    @Query("SELECT c FROM BDM_Client c ORDER BY c.id DESC LIMIT 1")
    Optional<BDM_Client> findTopByOrderByIdDesc();

    boolean existsByClientNameIgnoreCase(String clientName);


    @Query("SELECT c.clientName FROM BDM_Client c WHERE c.clientName = :clientName")
    List<String> findByClientName(String clientName);

    @Query("SELECT b FROM BDM_Client b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<BDM_Client> getClientsByCreatedAtRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    SELECT 
        r.job_id AS job_id,
        r.job_title AS job_title,
        r.client_name AS client_name,
        r.job_description AS job_description,
        r.job_description_blob AS job_description_blob,
        r.job_type AS job_type,
        r.location AS location,
        r.job_mode AS job_mode,
        r.experience_required AS experience_required,
        r.notice_period AS notice_period,
        r.relevant_experience AS relevant_experience,
        r.qualification AS qualification,
        r.salary_package AS salary_package,
        r.no_of_positions AS no_of_positions,
        r.requirement_added_time_stamp AS requirement_added_time_stamp,
        GROUP_CONCAT(jr.recruiter_id) AS recruiter_id,
        r.status AS status,
        GROUP_CONCAT(u2.user_name) AS recruiter_name,
        r.assigned_by AS assigned_by
    FROM requirements_model r
    JOIN bdm_client b 
        ON TRIM(UPPER(r.client_name)) COLLATE utf8mb4_bin = TRIM(UPPER(b.client_name)) COLLATE utf8mb4_bin
    JOIN user_details u 
        ON b.on_boarded_by = u.user_name
    LEFT JOIN job_recruiters jr 
        ON jr.job_id = r.job_id
    LEFT JOIN user_details u2 
        ON jr.recruiter_id = u2.user_id
    WHERE u.user_id = :userId
      AND r.requirement_added_time_stamp >= :startDateTime
      AND r.requirement_added_time_stamp <= :endDateTime
    GROUP BY r.job_id
""", nativeQuery = true)
    List<Tuple> findRequirementsByBdmUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT b FROM BDM_Client b")
    List<BDM_Client> getClients();
}
