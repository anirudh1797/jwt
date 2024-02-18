package com.spring.jwt.security.services;

import com.spring.jwt.models.ERole;
import com.spring.jwt.models.Role;
import com.spring.jwt.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        roleRepository.save(new Role(1, ERole.ROLE_ADMIN));
        roleRepository.save(new Role(2, ERole.ROLE_PROFESSOR));
        roleRepository.save(new Role(3, ERole.ROLE_STUDENT));
    }
}