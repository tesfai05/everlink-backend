package com.tesfai.everlink.config;

import com.tesfai.everlink.entity.Role;
import com.tesfai.everlink.entity.User;
import com.tesfai.everlink.repository.IRoleRepository;
import com.tesfai.everlink.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class StartupAdminLoader {
    @Value(value = "${admin.username}")
    private String adminUsername;
    @Value(value = "${admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner loadDefaultAdmin(IUserRepository userRepository, PasswordEncoder passwordEncoder, IRoleRepository roleRepository) {
        return args -> {
            boolean adminExists = userRepository
                    .findAll()
                    .stream()
                    .map(user -> user.getRoles())
                    .flatMap(u->u.stream().map(us->us.getName()))
                    .anyMatch(user -> "ADMIN".equalsIgnoreCase(user));

            if (!adminExists) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                Role userRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                admin.setRoles(Set.of(userRole));
                userRepository.save(admin);
            }
        };
    }
}
