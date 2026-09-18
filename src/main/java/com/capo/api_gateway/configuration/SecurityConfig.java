package com.capo.api_gateway.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Value("${cors.allowed-origins:http://localhost:4200,https://bench-frontend.onrender.com}")
	private String allowedOrigins;
	
	@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
	private String jwkSetUri;
	
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchanges -> exchanges
        	.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .pathMatchers("/actuator/health", 
            	    "/public/**", 
            	    "/oauth2/**", 
            	    "/login", 
            	    "/login/**", 
            	    "/register", 
            	    "/register/**", 
            	    "/logout", 
            	    "/logout/**", 
            	    "/.well-known/**").permitAll() // Public paths
            .anyExchange().authenticated() // All routed microservice requests require auth
        )
        .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwkSetUri(jwkSetUri))
            );

		return http.build();
	}
	
	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "https://*.onrender.com"));
        for (String origin : origins) {
            config.addAllowedOrigin(origin);
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
