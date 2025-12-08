package com.app.bookamenities.configuration;

import com.app.bookamenities.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    //config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedOrigins(Collections.singletonList("*"));
                    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/", "/index.html", "/assets/**", "/static/**", "/images/**").permitAll()
                        .requestMatchers("/book-amenities/login", "/book-amenities/user").permitAll()
                        .requestMatchers("/book-amenities/**").authenticated())
//                .exceptionHandling(ex -> ex
//                        .accessDeniedHandler(customAccessDeniedHandler())
//                        .authenticationEntryPoint(customAuthenticationEntryPoint()))
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
//        return (req, res, ex) -> {
//            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            res.setContentType("application/json");
//            res.getWriter().write("{\"error\": \"401 - Unauthorized: Please login again\"}");
//        };
//    }
//
//    @Bean
//    public AccessDeniedHandler customAccessDeniedHandler() {
//        return (req, res, ex) -> {
//            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            res.setContentType("application/json");
//            res.getWriter().write("{\"error\": \"403 - No Access: Please login again\"}");
//        };
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
