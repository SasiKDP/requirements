package com.dataquadinc.repository;



import com.dataquadinc.model.BDM_Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BDM_Repo extends JpaRepository<BDM_Client,String> {

    boolean existsByClientNameIgnoreCase(String clientName);


    @Query("SELECT c.clientName FROM BDM_Client c WHERE c.clientName = :clientName")
    List<String> findByClientName(String clientName);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(id, 7) AS UNSIGNED)) FROM BDM_Client_prod WHERE id LIKE 'CLIENT%'", nativeQuery = true)
    Integer findMaxClientNumber();

}