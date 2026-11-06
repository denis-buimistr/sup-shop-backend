package com.supshop.sup_shop_backend.config;

import com.supshop.sup_shop_backend.model.Role;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.RoleRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminInitializer(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.username}") String username,
                            @Value("${app.admin.email}") String email,
                            @Value("${app.admin.password}") String password) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            return;
        }
        if (userRepository.existsByRoles_Name(ADMIN_ROLE)) {
            return;
        }
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            log.warn("Admin not created: user '{}' or email '{}' already exists", username, email);
            return;
        }

        Role adminRole = roleRepository.findByName(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role " + ADMIN_ROLE + " not found"));

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);

        log.info("Initial admin '{}' created", username);
    }
}