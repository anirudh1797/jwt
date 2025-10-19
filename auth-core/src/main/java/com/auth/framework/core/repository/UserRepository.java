package com.auth.framework.core.repository;

import com.auth.framework.core.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Follows Single Responsibility Principle - only handles user data access.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin, u.updatedAt = :updatedAt WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, 
                        @Param("lastLogin") LocalDateTime lastLogin, 
                        @Param("updatedAt") LocalDateTime updatedAt);
    
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts, u.updatedAt = :updatedAt WHERE u.id = :userId")
    void updateFailedLoginAttempts(@Param("userId") Long userId, 
                                  @Param("attempts") Integer attempts, 
                                  @Param("updatedAt") LocalDateTime updatedAt);
    
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil, u.updatedAt = :updatedAt WHERE u.id = :userId")
    void updateLockedUntil(@Param("userId") Long userId, 
                          @Param("lockedUntil") LocalDateTime lockedUntil, 
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
}