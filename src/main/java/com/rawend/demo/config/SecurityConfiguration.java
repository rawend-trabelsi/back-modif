package com.rawend.demo.config;

import com.rawend.demo.services.UserService;
import com.rawend.demo.config.JWTAuthenticationFilter;
import com.rawend.demo.entity.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Arrays;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  
            .authorizeRequests()
            	.requestMatchers("/api/v1/auth/user/**").authenticated()
                .requestMatchers("/api/v1/auth/signin", "/api/v1/auth/signup", "/api/v1/auth/refresh", "/api/v1/auth/forgot-password","/api/v1/auth/verify-code", "/api/v1/auth/check-email","/api/v1/auth/check-phone","/api/v1/auth/reset-password","/api/v1/auth/resend-verification-code","/home","/auth/facebook","/api/v1/auth/user/update-profile","/api/v1/auth/user/profile").permitAll()
                .requestMatchers("/api/v1/auth/admin").hasAuthority(Role.ADMIN.name())
                .requestMatchers("/api/v1/auth/Technicien").hasAuthority(Role.TECHNICIEN.name())
                .requestMatchers("/api/promotions/**").hasAuthority(Role.ADMIN.name())
                .requestMatchers("/api/promotions/**").hasAuthority(Role.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/promotions/apply", "/api/promotions/servicesWithPromotions").hasAnyRole("USER", "ADMIN")

                .requestMatchers("/api/v1/auth/admin/**")
                .hasAuthority(Role.ADMIN.name())
                
        
                .requestMatchers(HttpMethod.PUT, "/api/services/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasAuthority("ADMIN") // Only ADMIN can delete services
                .requestMatchers(HttpMethod.POST, "/api/services/**").hasAuthority("ADMIN") // Only ADMIN can create services
                .requestMatchers(HttpMethod.GET, "/api/services/**").hasAnyAuthority("ADMIN", "USER")
                
                .requestMatchers(HttpMethod.PUT, "/api/abonnements/**").hasAuthority("ADMIN")  // Only ADMIN can update abonnements
                .requestMatchers(HttpMethod.DELETE, "/api/abonnements/**").hasAuthority("ADMIN") // Only ADMIN can delete abonnements
                .requestMatchers(HttpMethod.POST, "/api/abonnements/**").hasAuthority("ADMIN") // Only ADMIN can create abonnements
                .requestMatchers(HttpMethod.GET, "/api/abonnements/**").hasAnyAuthority("ADMIN", "USER") // Both ADMIN and USER can view abonnements


                .requestMatchers("/api/v1/auth/user").hasAuthority(Role.USER.name())
              
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));  // Autorise toutes les origines
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
}
