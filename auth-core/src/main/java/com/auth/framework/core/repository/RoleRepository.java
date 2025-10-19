package com.auth.framework.core.repository;

import com.auth.framework.core.domain.ERole;
import com.auth.framework.core.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity operations.
 * Follows Single Responsibility Principle - only handles role data access.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(ERole name);
    
    boolean existsByName(ERole name);
}