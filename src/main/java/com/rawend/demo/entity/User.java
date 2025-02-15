package com.rawend.demo.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rawend.demo.entity.AuthProvider; // Assurez-vous que cette enum existe bien
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "phone")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password; // Peut être null pour Facebook

    private String confirmPassword;  // Pas stocké en DB

    @Enumerated(EnumType.STRING)
    private Role role;

    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Email should be valid and contain '@'")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8}$", message = "Phone number must be up to 8 digits")
    @Column(unique = true, nullable = true) // Peut être NULL pour Facebook
    private String phone;

    @Column(nullable = true)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider; // Facebook ou Local

    // Méthode de validation pour gérer les contraintes dynamiquement
    @PrePersist
    @PreUpdate
    public void validateUser() {
        if (authProvider == AuthProvider.LOCAL) {
            if (password == null || password.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters long");
            }
            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("Phone number is required");
            }
        }
    }

    // Security methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return email != null; 
    }

   

    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsernameValue() { return username; }
    public void setUsername(String username) { this.username = username; }
    @Override
    public String getPassword() {
        
        return authProvider == AuthProvider.FACEBOOK ? null : password;
    }

    @Override
    public String getUsername() {
        return email; 
    }

    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public String getVerificationCode() { return verificationCode; }

    public AuthProvider getAuthProvider() { return authProvider; }
    public void setAuthProvider(AuthProvider authProvider) { this.authProvider = authProvider; }

    // Vérifier la correspondance des mots de passe (uniquement pour LOCAL)
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
    public String getUsernameFieldDirectly() {
        return this.username; // Retourne le vrai champ username
    }
}
