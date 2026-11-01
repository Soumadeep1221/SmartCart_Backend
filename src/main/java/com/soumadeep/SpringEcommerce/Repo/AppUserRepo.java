package com.soumadeep.SpringEcommerce.Repo;

import com.soumadeep.SpringEcommerce.Entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AppUserRepo extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser u SET u.tokenBalance = u.tokenBalance - :cost " +
            "WHERE u.email = :email AND u.tokenBalance >= :cost")
    int deductTokens(String email, int cost);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser u SET u.tokenBalance = u.tokenBalance + :cost WHERE u.email = :email")
    void addTokens(String email, int cost);

    @Query("SELECT u.tokenBalance FROM AppUser u WHERE u.email = :email")
    Optional<Integer> findTokenBalance(String email);
}