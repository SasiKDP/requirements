package com.dataquadinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dataquadinc.model.RequirementsModel;

@Repository
public interface RequirementsDao extends JpaRepository<RequirementsModel, String>
{

	
}
