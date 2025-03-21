package com.dataquadinc.repository;



import com.dataquadinc.model.BDM_Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BDM_Repo extends JpaRepository<BDM_Client,String> {
    @Query("SELECT c FROM BDM_Client c ORDER BY c.id DESC LIMIT 1")
    Optional<BDM_Client> findTopByOrderByIdDesc();

    boolean existsByClientNameIgnoreCase(String clientName);


    @Query("SELECT c.clientName FROM BDM_Client c WHERE c.clientName = :clientName")
    List<String> findByClientName(String clientName);
}