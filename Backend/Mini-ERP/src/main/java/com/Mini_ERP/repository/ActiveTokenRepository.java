package com.Mini_ERP.repository;

import com.Mini_ERP.model.ActiveToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ActiveTokenRepository extends JpaRepository<ActiveToken, String> {

    Optional<ActiveToken> findByJtiAndRevokedFalse(String jti);

    @Modifying
    @Query("""
            UPDATE ActiveToken t
            SET t.revoked = true
            WHERE t.user.id = :userId AND t.revoked = false
            """)
    int revokeAllByUserId(@Param("userId") Long userId);
}