package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.pi_back.entities.enums.UserType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class User  implements UserDetails
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "fullName is required")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password; // sera hashé

    @NotNull(message = "userType is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Pattern(regexp = "^[0-9]{8}$", message = "phone must contain 8 digits")
    private String phone;


    @Column(nullable = false, columnDefinition = "bit default 1")
    private Boolean enabled;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (enabled == null) enabled = true;
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

    @Override
    public String getUsername() {
        return email; // login = email
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    private Boolean twoFactorEnabled = false;

    private String otpCode;

    private LocalDateTime otpExpiry;
}