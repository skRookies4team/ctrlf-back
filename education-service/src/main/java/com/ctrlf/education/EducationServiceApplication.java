package com.ctrlf.education;

import com.ctrlf.common.security.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Import(SecurityConfig.class)
@EnableFeignClients
@EntityScan(basePackages = "com.ctrlf.education")
@EnableJpaRepositories(basePackages = "com.ctrlf.education")
public class EducationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationServiceApplication.class, args);
    }
}