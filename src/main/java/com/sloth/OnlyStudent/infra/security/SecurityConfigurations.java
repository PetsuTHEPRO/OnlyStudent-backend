package com.sloth.OnlyStudent.infra.security;

import java.util.List;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfigurations {

    @Autowired
    SecurityFilter securityFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.auth.rate-limit.requests:10}")
    private int authRateLimitRequests;

    @Value("${app.auth.rate-limit.window-seconds:60}")
    private long authRateLimitWindowSeconds;

    @Bean
    public AuthRateLimitFilter authRateLimitFilter() {
        return new AuthRateLimitFilter(authRateLimitRequests, Duration.ofSeconds(authRateLimitWindowSeconds));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Adiciona a configuração de CORS
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("text/plain");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("Autenticação necessária.");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("text/plain");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("Acesso negado.");
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
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
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/turma/register").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.POST, "/turma/*/entrar").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/turma/turmasAluno").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/turma/studentTurmas").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/turma/educatorTurmas").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.GET, "/turma/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/turma/**").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.DELETE, "/turma/**").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.POST, "/material/**").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.DELETE, "/material/**").hasRole("EDUCATOR")
                        .requestMatchers(HttpMethod.POST, "/support/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/token/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authRateLimitFilter(), SecurityContextHolderFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
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