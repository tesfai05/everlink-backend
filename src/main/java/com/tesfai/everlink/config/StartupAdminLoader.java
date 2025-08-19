package com.tesfai.everlink.config;

import com.tesfai.everlink.entity.Role;
import com.tesfai.everlink.entity.User;
import com.tesfai.everlink.repository.IRoleRepository;
import com.tesfai.everlink.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.logging.Logger;

@Configuration
public class StartupAdminLoader {
    private final Logger LOG = Logger.getLogger("StartupAdminLoader");
    @Value(value = "${admin.username}")
    private String adminUsername;
    @Value(value = "${admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner loadDefaultAdmin(IUserRepository userRepository, PasswordEncoder passwordEncoder, IRoleRepository roleRepository, JdbcTemplate jdbcTemplate) {
        return args -> {
            // Insert SUPER_ADMIN if it doesn't exist
            jdbcTemplate.update("""
                INSERT INTO wcnua0h21v69edfi.roles (name)
                SELECT 'SUPER_ADMIN'
                WHERE NOT EXISTS (
                    SELECT 1 FROM wcnua0h21v69edfi.roles WHERE name = 'SUPER_ADMIN'
                )
            """);
            // Insert ADMIN if it doesn't exist
            jdbcTemplate.update("""
                INSERT INTO wcnua0h21v69edfi.roles (name)
                SELECT 'ADMIN'
                WHERE NOT EXISTS (
                    SELECT 1 FROM wcnua0h21v69edfi.roles WHERE name = 'ADMIN'
                )
            """);

            // Insert USER if it doesn't exist
            jdbcTemplate.update("""
                INSERT INTO wcnua0h21v69edfi.roles (name)
                SELECT 'USER'
                WHERE NOT EXISTS (
                    SELECT 1 FROM wcnua0h21v69edfi.roles WHERE name = 'USER'
                )
            """);

            boolean adminExists = userRepository
                    .findAll()
                    .stream()
                    .filter(user -> user!=null)
                    .map(user -> user.getRoles())
                    .filter(roles->roles!=null && roles.size()>0)
                    .flatMap(u->u.stream().map(us->us.getName()))
                    .anyMatch(user -> "SUPER_ADMIN".equalsIgnoreCase(user));

            if (!adminExists) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                Role userRole = roleRepository.findByName("SUPER_ADMIN")
                        .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));
                admin.setRoles(Set.of(userRole));
                admin.setMemberId("SA123");
                admin.setEnabled(true);
                userRepository.save(admin);
                LOG.info(admin.getUsername() + " super admin inserted.");
            }
        };
    }
}
