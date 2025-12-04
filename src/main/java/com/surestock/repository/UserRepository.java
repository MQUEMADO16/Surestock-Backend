package com.surestock.repository;

import com.surestock.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByBusinessId(Long businessId);

    /**
     * Deletes all users (e.g., Owners/Employees) belonging to a specific business ID.
     */
    @Modifying
    @Transactional
    void deleteByBusinessId(Long businessId);
}