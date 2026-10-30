package com.FlowPay.transaction;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry,String> {

 // want a database lock for that particular transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from LedgerEntry e where e.id=:id")
    Optional<LedgerEntry> lockById(@Param("id") String id);


    // Give  the entries by the merchant id and sorted by the latest one that are created
    List<LedgerEntry>  findByMerchantIdOrderByCreatedAtDesc(String Key);



}

