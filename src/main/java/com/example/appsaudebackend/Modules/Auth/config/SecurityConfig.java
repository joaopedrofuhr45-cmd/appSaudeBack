package com.example.appsaudebackend.Modules.Auth.config;

import com.example.appsaudebackend.Modules.Auth.Service.AuthUserDetailsService;
import com.example.appsaudebackend.Modules.Auth.filter.JwtAuthenticationFilter;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Getter @Setter @RequiredArgsConstructor
public class SecurityConfig {
    private final AuthUserDetailsService authUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/cadastro").permitAll()
                .requestMatchers("/auth/logout", "/auth/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/consultas").hasAnyRole("ATENDENTE", "MEDICO")
                .requestMatchers(HttpMethod.GET, "/consultas/paciente/**").hasRole("USUARIO")
                .requestMatchers(HttpMethod.POST, "/consultas").hasAnyRole("USUARIO", "ATENDENTE")
                .requestMatchers(HttpMethod.PATCH, "/consultas/*/finalizar").hasRole("MEDICO")
                .requestMatchers(HttpMethod.GET, "/consultas/*").hasAnyRole("USUARIO", "ATENDENTE", "MEDICO")
                .requestMatchers(HttpMethod.GET, "/medicos", "/especialidades").hasAnyRole("USUARIO", "ATENDENTE", "MEDICO")
                .requestMatchers("/medicos/**").hasRole("MEDICO")
                .requestMatchers("/atendentes/**").hasRole("ATENDENTE")
                .requestMatchers("/pacientes").hasRole("ATENDENTE")
                .requestMatchers("/usuarios/**").hasRole("USUARIO")
                .requestMatchers("/conta/**").authenticated()
                .anyRequest().authenticated())
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(authUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}