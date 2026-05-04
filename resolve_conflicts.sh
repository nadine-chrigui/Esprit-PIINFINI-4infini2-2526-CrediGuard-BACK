#!/bin/bash

echo "=== Etape 1: Resolution des conflits - version yassmine ==="
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/AuthController.java
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/CartController.java
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/CategoryController.java
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/DeliveryAddressController.java
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/DeliveryController.java
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/OrderController.java
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/ProductController.java
git checkout --ours src/main/java/tn/esprit/pi_back/controllers/PromoCodeController.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/AuthRequest.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/AuthResponse.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/LoginResponse.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/UpdateProfileRequest.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/UpdateUserRequest.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/cart/CartItemResponse.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/cart/CartResponse.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/order/OrderCreateRequest.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/order/OrderItemResponse.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/product/ProductCreateRequest.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/product/ProductResponse.java
git checkout --ours src/main/java/tn/esprit/pi_back/dto/product/ProductUpdateRequest.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/Credit.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/DecisionCredit.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/DemandeCredit.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/Echeance.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/EvaluationRisque.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/Order.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/Payment.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/PlanUtilisationCredit.java
git checkout --ours src/main/java/tn/esprit/pi_back/entities/Product.java
git checkout --ours src/main/java/tn/esprit/pi_back/exceptions/GlobalExceptionHandler.java
git checkout --ours src/main/java/tn/esprit/pi_back/repositories/DeliveryRepository.java
git checkout --ours src/main/java/tn/esprit/pi_back/repositories/OrderItemRepository.java
git checkout --ours src/main/java/tn/esprit/pi_back/repositories/OrderRepository.java
git checkout --ours src/main/java/tn/esprit/pi_back/repositories/ProductRepository.java
git checkout --ours src/main/java/tn/esprit/pi_back/repositories/UserRepository.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/CartServiceImpl.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/CategoryServiceImpl.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/DeliveryService.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/DeliveryServiceImpl.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/EmailServiceImpl.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/OrderService.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/OrderServiceImpl.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/ProductService.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/ProductServiceImpl.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/UserService.java
git checkout --ours src/main/java/tn/esprit/pi_back/services/UserServiceImpl.java

echo "=== Etape 2: Ecriture des fichiers fusionnes manuellement ==="

# pom.xml
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.2</version>
        <relativePath/>
    </parent>

    <groupId>tn.esprit</groupId>
    <artifactId>Pi_back</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <n>Pi_back</n>
    <description>Pi_back</description>

    <properties>
        <java.version>17</java.version>
        <lombok.version>1.18.32</lombok.version>
        <jjwt.version>0.11.5</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# application.properties
cat > src/main/resources/application.properties << 'EOF'
spring.application.name=Pi_back
server.port=8089
server.servlet.context-path=/api

### DATABASE ###
spring.datasource.url=jdbc:mysql://localhost:3306/my_CrediGuard?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=

### JPA / HIBERNATE ###
spring.main.allow-bean-definition-overriding=true
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true

logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping=TRACE
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.springframework.transaction=TRACE

### MAIL ###
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=megblifamily@gmail.com
spring.mail.password=hulx udry pzzl chok
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

### FLASK CREDIT RISK ###
credit-risk.flask.base-url=http://localhost:5000
EOF

# UserType.java
cat > src/main/java/tn/esprit/pi_back/entities/enums/UserType.java << 'EOF'
package tn.esprit.pi_back.entities.enums;

public enum UserType {
    ADMIN,
    BENEFICIARY,
    PARTNER,
    CLIENT,
    INSURANCE
}
EOF

# User.java
cat > src/main/java/tn/esprit/pi_back/entities/User.java << 'EOF'
package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.entities.enums.PartnerType;
import tn.esprit.pi_back.entities.enums.PartnerStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString @EqualsAndHashCode
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "fullName is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull(message = "userType is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    private PartnerType partnerType;

    @Enumerated(EnumType.STRING)
    private PartnerStatus partnerStatus;

    @Pattern(regexp = "^[24579][0-9]{7}$", message = "phone must be a valid Tunisian number (8 digits)")
    @Column(name = "phone")
    private String phone;

    @Column(name = "enabled", nullable = false, columnDefinition = "bit default 1")
    private Boolean enabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "two_factor_enabled", nullable = false, columnDefinition = "bit default 0")
    private Boolean twoFactorEnabled;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @PrePersist
    protected void onCreate() {
        if (enabled == null) enabled = true;
        if (twoFactorEnabled == null) twoFactorEnabled = false;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));
    }

    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return Boolean.TRUE.equals(enabled); }
}
EOF

# InsuranceClaim.java
cat > src/main/java/tn/esprit/pi_back/entities/InsuranceClaim.java << 'EOF'
package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString @EqualsAndHashCode
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String claimReference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status = ClaimStatus.PENDING;

    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", unique = true, nullable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy insurancePolicy;

    @Column(nullable = false)
    private int riskScore;

    @Column(length = 20)
    private String analysis;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ClaimStatus.PENDING;
    }
}
EOF

# SecurityConfig.java
cat > src/main/java/tn/esprit/pi_back/config/SecurityConfig.java << 'EOF'
package tn.esprit.pi_back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/projects/**").hasAnyRole("ADMIN", "PARTNER")
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/seller/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profils-credit/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/profils-credit/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/profils-credit/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/demandes").authenticated()
                        .requestMatchers("/api/evaluations/**").authenticated()
                        .requestMatchers("/api/profils-credit/by-client").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
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
EOF

# JwtAuthFilter.java
cat > src/main/java/tn/esprit/pi_back/security/JwtAuthFilter.java << 'EOF'
package tn.esprit.pi_back.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractSubject(token);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/auth/")
                || path.startsWith("/api/auth/")
                || path.startsWith("/api/vouchers")
                || path.startsWith("/api/products")
                || path.startsWith("/api/cart")
                || path.startsWith("/api/categories")
                || path.startsWith("/api/insurance/claims")
                || path.startsWith("/api/insurance/policies");
    }
}
EOF

# UserController.java
cat > src/main/java/tn/esprit/pi_back/controllers/UserController.java << 'EOF'
package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.ClientOptionDTO;
import tn.esprit.pi_back.dto.ProfileResponse;
import tn.esprit.pi_back.dto.UpdateProfileRequest;
import tn.esprit.pi_back.dto.UpdateUserRequest;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.entities.enums.PartnerType;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.create(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAll(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) UserType userType) {
        return ResponseEntity.ok(userService.getAll(enabled, userType));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        return ResponseEntity.ok(userService.updateEnabled(id, enabled));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    @GetMapping("/me-full")
    public ResponseEntity<User> getCurrentUserFull() {
        return ResponseEntity.ok(userService.getCurrentUserOrThrow());
    }

    @GetMapping("/clients")
    public List<ClientOptionDTO> getClients() {
        return userRepository.findByUserType(UserType.CLIENT)
                .stream()
                .map(user -> new ClientOptionDTO(user.getId(), user.getFullName(), user.getEmail()))
                .toList();
    }

    @GetMapping("/partners")
    public ResponseEntity<List<User>> getPartners() {
        return ResponseEntity.ok(userService.getPartners());
    }

    @GetMapping("/partners/type/{type}")
    public ResponseEntity<List<User>> getPartnersByType(@PathVariable String type) {
        return ResponseEntity.ok(userService.getPartnersByType(PartnerType.valueOf(type)));
    }
}
EOF

echo "=== Etape 3: git add -A ==="
git add -A

echo "=== Etape 4: Commit ==="
git commit -m "merge: integration/mokaddem-yassmine + main -> final integration"

echo "=== Etape 5: Push sur main ==="
git push origin merge/final-integration:main --force-with-lease

echo "=== TERMINE ! ==="
