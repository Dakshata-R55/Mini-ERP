package com.Mini_ERP.repository;

import com.Mini_ERP.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.modulePermissions WHERE u.loginId = :loginId")
    Optional<AppUser> findByLoginIdWithPermissions(@Param("loginId") String loginId);
}