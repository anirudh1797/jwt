package com.spring.jwt.repository;

import com.spring.jwt.models.ERole;
import com.spring.jwt.models.Role;
import com.spring.jwt.models.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {

}
