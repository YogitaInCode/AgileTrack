
package com.agiletrack.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            SessionUserFilter sessionUserFilter) throws Exception {

        return http
            .csrf(csrf -> csrf.disable())
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(e ->
                e.authenticationEntryPoint((req, res, ex) ->
                    res.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Please sign in"
                    )))
            .authorizeHttpRequests(auth -> auth

                // Public pages and authentication
                .requestMatchers(
                    "/", "/index.html", "/login.html",
                    "/style.css", "/app.js", "/login.js",
                    "/favicon.ico", "/h2-console/**",
                    "/api/auth/**"
                ).permitAll()

                // Project management
                .requestMatchers(
                    HttpMethod.POST, "/api/projects/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                .requestMatchers(
                    HttpMethod.PUT, "/api/projects/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/projects/**"
                ).hasRole("ADMIN")

                // Sprint management
                .requestMatchers(
                    HttpMethod.POST, "/api/sprints/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                .requestMatchers(
                    HttpMethod.PUT, "/api/sprints/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/sprints/**"
                ).hasRole("ADMIN")

                // Task management
                .requestMatchers(
                    HttpMethod.POST, "/api/tasks/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                .requestMatchers(
                    HttpMethod.PUT, "/api/tasks/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER", "DEVELOPER")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/tasks/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                // Bug tracking
                .requestMatchers(
                    HttpMethod.POST, "/api/bugs/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER", "DEVELOPER", "TESTER")

                .requestMatchers(
                    HttpMethod.PUT, "/api/bugs/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER", "DEVELOPER", "TESTER")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/bugs/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                // QA test cases
                .requestMatchers(
                    HttpMethod.POST, "/api/tests/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER", "TESTER")

                .requestMatchers(
                    HttpMethod.PUT, "/api/tests/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER", "TESTER")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/tests/**"
                ).hasAnyRole("ADMIN", "PROJECT_MANAGER")

                // Team directory
                .requestMatchers("/api/team/**")
                    .hasAnyRole("ADMIN", "PROJECT_MANAGER")

                // Remaining API endpoints require login
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            )
            .addFilterBefore(
                sessionUserFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .build();
    }
}
