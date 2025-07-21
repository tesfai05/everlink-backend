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
            // Insert ADMIN if it doesn't exist
            jdbcTemplate.update("""
                INSERT INTO everlinkllc.roles (name)
                SELECT 'ADMIN'
                WHERE NOT EXISTS (
                    SELECT 1 FROM everlinkllc.roles WHERE name = 'ADMIN'
                )
            """);

            // Insert USER if it doesn't exist
            jdbcTemplate.update("""
                INSERT INTO everlinkllc.roles (name)
                SELECT 'USER'
                WHERE NOT EXISTS (
                    SELECT 1 FROM everlinkllc.roles WHERE name = 'USER'
                )
            """);

            LOG.info("ADMIN & USER role name inserted.");
            boolean adminExists = userRepository
                    .findAll()
                    .stream()
                    .filter(user -> user!=null)
                    .map(user -> user.getRoles())
                    .filter(roles->roles!=null && roles.size()>0)
                    .flatMap(u->u.stream().map(us->us.getName()))
                    .anyMatch(user -> "ADMIN".equalsIgnoreCase(user));

            if (!adminExists) {
                LOG.info("About to insert admin.");
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                Role userRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                admin.setRoles(Set.of(userRole));
                admin.setMemberId("AD123");
                admin.setEnabled(true);
                userRepository.save(admin);
                LOG.info(admin.getUsername() + " admin inserted.");
            }
        };
    }
}
