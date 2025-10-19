package com.auth.framework.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Demo application showcasing the authentication framework.
 * This is a sample application that demonstrates how to use the auth framework.
 */
@SpringBootApplication(scanBasePackages = {
    "com.auth.framework.core",
    "com.auth.framework.demo"
})
@EntityScan("com.auth.framework.core.domain")
@EnableJpaRepositories("com.auth.framework.core.repository")
@EnableAsync
@EnableScheduling
public class AuthDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthDemoApplication.class, args);
    }
}