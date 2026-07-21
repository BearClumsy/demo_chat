package com.example.demo_chat.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers(HttpMethod.POST, "/api/users")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .httpBasic(withDefaults())
        .build();
  }
}
