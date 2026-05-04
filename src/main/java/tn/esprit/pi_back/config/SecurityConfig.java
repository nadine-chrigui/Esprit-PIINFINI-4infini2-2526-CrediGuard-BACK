package tn.esprit.pi_back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.esprit.pi_back.security.JwtAuthFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/auth/**", "/api/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/profils-credit/me", "/api/profils-credit/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/profils-credit/me", "/api/profils-credit/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/profils-credit/me", "/api/profils-credit/me").authenticated()
                        .requestMatchers("/profils-credit/by-client", "/api/profils-credit/by-client").authenticated()
                        .requestMatchers("/evaluations/**", "/api/evaluations/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/demandes", "/api/demandes").authenticated()
                        .requestMatchers("/decisions/**", "/credits/**").authenticated()

                        .requestMatchers("/users/me", "/users/me-full").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/clients", "/users/partners", "/users/partners/type/**").authenticated()
                        .requestMatchers("/api/projects/**").hasAnyRole("ADMIN", "PARTNER")

                        .requestMatchers("/insurance/**", "/vouchers/**", "/partnership/**", "/score-risque/**", "/notifications/**").permitAll()
                        .requestMatchers("/contrats/**", "/claims/**", "/assureurs/**", "/offres/**").permitAll()
                        .requestMatchers("/products/**", "/categories/**", "/cart/**").permitAll()
                        .requestMatchers("/api/products/**", "/api/categories/**", "/api/cart/**").permitAll()
                        .requestMatchers("/partner-products/**").permitAll()
                        .requestMatchers("/profils-credit/**", "/sms/**").permitAll()
                        .requestMatchers("/modalites/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
