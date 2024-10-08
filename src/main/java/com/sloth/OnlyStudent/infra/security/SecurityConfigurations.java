package com.sloth.OnlyStudent.infra.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Adiciona a configuração de CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                		// Rotas Públicassss
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                        // Rotas do Estudante
                        .requestMatchers(HttpMethod.POST, "/student/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/student/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.PUT, "/student/**").hasRole("STUDENT")
                        // Rotas do Professor
                        .requestMatchers(HttpMethod.POST, "/educator/**").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.PUT, "/educator/**").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.GET, "/educator/**").hasRole("EDUCATOR")
                        // Rotas de Estudamte e Professor
                        .requestMatchers(HttpMethod.POST, "/turma/**").hasAnyRole("EDUCATOR", "STUDENT")
                        .requestMatchers(HttpMethod.GET, "/turma/**").hasAnyRole("EDUCATOR", "STUDENT")
                        .requestMatchers(HttpMethod.PUT, "/turma/**").hasAnyRole("EDUCATOR", "STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "turma/**").hasAnyRole("EDUCATOR", "STUDENT")
                        .requestMatchers(HttpMethod.POST, "/material/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/material/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/support/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/token/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://onlystudy.netlify.app", "http://localhost:5173", "http://192.168.2.43:5173", "192.168.2.43:5173")); // Adicione suas origens aqui
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos permitidos
        configuration.setAllowedHeaders(List.of("*")); // Cabeçalhos permitidos
        configuration.setAllowCredentials(true); // Permite envio de cookies e credenciais

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}