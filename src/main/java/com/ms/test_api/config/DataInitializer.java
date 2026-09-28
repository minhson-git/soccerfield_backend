package com.ms.test_api.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ms.test_api.entity.Role;
import com.ms.test_api.entity.User;
import com.ms.test_api.entity.enums.RoleName;
import com.ms.test_api.repository.RoleRepository;
import com.ms.test_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-username:}")
    private String adminUsername;

    @Value("${app.bootstrap.admin-password:}")
    private String adminPassword;

    @Value("${app.bootstrap.admin-email:}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                log.info("Seeding role {}", roleName);
                return roleRepository.save(role);
            });
        }

        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)) {
            log.info("No bootstrap admin configured; skipping admin creation");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName("System Administrator");
        admin.setEnabled(true);
        admin.setRole(adminRole);

        userRepository.save(admin);
        log.warn("Bootstrap admin '{}' created. Change this password immediately.", adminUsername);
    }
}