package com.skylink.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public OtpEnforcementFilter otpEnforcementFilter() {
        return new OtpEnforcementFilter();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        repository.setCreateTableOnStartup(false);
        return repository;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http, CustomAuthenticationProvider authProvider) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .authenticationProvider(authProvider)
            .build();
    }

    @Bean
    public SecurityFilterChain filterChain(
             HttpSecurity http,
             CustomAuthenticationSuccessHandler successHandler,
             PersistentTokenRepository tokenRepository,
             OtpEnforcementFilter otpEnforcementFilter) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/signup", "/verify-otp", "/verify-otp/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/error/**").permitAll()
                .requestMatchers("/superadmin/**").hasRole("SUPER_ADMIN")
                .requestMatchers(
                    "/admin/users/**", "/reports/**",
                    "/flights/add", "/flights/edit/**", "/flights/delete/**",
                    "/aircraft/add", "/aircraft/edit/**", "/aircraft/delete/**",
                    "/customers/delete/**"
                ).hasRole("ADMIN")
                .requestMatchers("/payment/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(
                    "/dashboard", "/flights", "/flights/{id}",
                    "/aircraft", "/aircraft/{id}", "/bookings/**",
                    "/customers", "/customers/{id}", "/customers/add", "/customers/edit/**"
                ).hasAnyRole("ADMIN", "STAFF")
                .anyRequest().authenticated()
            )
             .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                 .permitAll()
             )
             .exceptionHandling(exception -> exception
                 .accessDeniedHandler(accessDeniedHandler)
             )
            .rememberMe(remember -> remember
                .tokenRepository(tokenRepository)
                .tokenValiditySeconds(7 * 24 * 60 * 60)
                .rememberMeParameter("remember-me")
                .key("skylink-remember-me-secret-key")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionConcurrency(concurrency -> concurrency
                    .maximumSessions(1)
                    .expiredUrl("/login?expired=true")
                )
            )
            .addFilterAfter(otpEnforcementFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
