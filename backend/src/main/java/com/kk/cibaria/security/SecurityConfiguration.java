package com.kk.cibaria.security;

import com.kk.cibaria.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpMethod;
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
public class SecurityConfiguration {

  private UserDetailService userDetailService;

  private JwtAuthenticationFilter authenticationFilter;

  public SecurityConfiguration(UserDetailService userDetailService, JwtAuthenticationFilter authenticationFilter) {
    this.userDetailService = userDetailService;
    this.authenticationFilter = authenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(req -> {
      req.requestMatchers("/api/authenticate", "/api/register").permitAll();
      req.requestMatchers("/authenticate", "/register").permitAll();
      req.requestMatchers(HttpMethod.GET, "/recipes/**").permitAll();
      req.requestMatchers(HttpMethod.POST, "/recipes").authenticated();
      req.requestMatchers(HttpMethod.PUT, "/recipes/**").authenticated(); 
      req.requestMatchers(HttpMethod.DELETE, "/recipes/**").authenticated();
      req.requestMatchers("/image/**").permitAll();
      req.requestMatchers("/api/recipes/**", "/api/image/**").permitAll();
      req.requestMatchers("/admin/**").hasRole("ADMIN");
      req.requestMatchers("/users/**").authenticated();
      req.anyRequest().authenticated();
    });

    http.csrf(csrf -> csrf.disable());
    http.cors(Customizer.withDefaults());
    http.httpBasic(Customizer.withDefaults());
    http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder());
    provider.setUserDetailsService(userDetailService);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(authenticationProvider());
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://192.168.68.109:4200"));
    corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "OPTIONS"));
    corsConfiguration.setAllowedHeaders(List.of("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }

}
