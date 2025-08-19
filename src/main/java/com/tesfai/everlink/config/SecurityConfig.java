package com.tesfai.everlink.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                 "/",
                                 "/js/**",
                                 "/css/**",
                                 "/image/**",
                                "/index.html",
                                "/html/signin.html",
                                "/html/signup.html",
                                "/html/footer.html",
                                "html/unauthorized.html",
                                "/html/forbidden.html",
                                "/api/v1/members/public/**",
                                "/html/changePassword.html",
                                "/docs/everlink_member_policy_tig.pdf",
                                "/docs/everlink_member_policy_en.pdf"
                        ).permitAll()
                        .requestMatchers("/api/v1/members/user/**", "/html/memberDetails.html","/html/spouse.html","/html/beneficiary.html").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/members/admin/**", "/html/register.html", "/html/list.html", "/html/email.html", "/html/admin.html").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
//                .exceptionHandling(e -> e
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.setContentType("application/json");
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
//                        })
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            response.setContentType("application/json");
//                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                            response.getWriter().write("{\"error\": \"Access Denied\"}");
//                        })
//                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/html/unauthorized.html"); // <-- your custom page
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/html/forbidden.html"); // <-- another custom page
                        })
                )

                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

