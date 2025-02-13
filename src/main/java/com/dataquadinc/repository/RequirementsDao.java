package com.dataquadinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dataquadinc.model.RequirementsModel_prod;

@Repository
public interface RequirementsDao extends JpaRepository<RequirementsModel_prod, String>
{
	
	@Query("SELECT r FROM RequirementsModel_prod r WHERE :recruiterId MEMBER OF r.recruiterIds")
	//SELECT j.* FROM requirements_model j JOIN job_recruiters jr ON j.job_id = jr.job_id WHERE jr.recruiter_id = 'recruiter_1';

    List<RequirementsModel_prod> findJobsByRecruiterId(String recruiterId);

	
}
