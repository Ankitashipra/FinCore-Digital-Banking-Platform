package com.fincore.account.repository;

import com.fincore.account.entity.Account;
import com.fincore.account.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByOwnerUserId(UUID ownerUserId);

    List<Account> findByStatus(AccountStatus status);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT a FROM Account a
       WHERE a.id = :accountId
       AND a.ownerUserId = :ownerUserId
       """)
    Optional<Account> findByIdAndOwnerUserIdForUpdate(
        @Param("accountId") UUID accountId,
        @Param("ownerUserId") UUID ownerUserId
);

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
       SELECT a FROM Account a
       WHERE a.id = :accountId
       """)
Optional<Account> findByIdForUpdate(
        @Param("accountId") UUID accountId
);
    
}