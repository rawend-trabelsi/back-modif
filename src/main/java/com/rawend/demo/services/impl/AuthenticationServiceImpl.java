package com.rawend.demo.services.impl;

import java.util.HashMap;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rawend.demo.dto.JWTAuthenticationResponse;
import com.rawend.demo.dto.RefreshTokenRequest;
import com.rawend.demo.dto.SignUpRequest;
import com.rawend.demo.dto.SigninRequest;
import com.rawend.demo.entity.Role;
import com.rawend.demo.entity.User;
import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.services.AuthenticationService;
import com.rawend.demo.services.JWTService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public User signup(SignUpRequest signupRequest) {
        // Vérifier que le mot de passe et la confirmation du mot de passe correspondent
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Vérifier si l'email existe déjà dans la base de données
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Vérifier si le numéro de téléphone existe déjà dans la base de données
        if (userRepository.existsByPhone(signupRequest.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        if (signupRequest.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Créer une nouvelle entité User et définir ses champs
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPhone(signupRequest.getPhone());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setConfirmPassword(passwordEncoder.encode(signupRequest.getConfirmPassword()));
        user.setRole(Role.USER); // Rôle par défaut en tant qu'USER

        // Enregistrer l'utilisateur dans la base de données
        return userRepository.save(user);
    }


    @Override
    public JWTAuthenticationResponse signin(SigninRequest signinRequest) {
        // Validate input
        if (signinRequest.getEmail() == null || signinRequest.getEmail().isEmpty() ||
            signinRequest.getPassword() == null || signinRequest.getPassword().isEmpty()) {
            throw new BadCredentialsException("Email and password must not be empty");
        }

        // Retrieve the user from the database by email
        User user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email does not exist"));

        // Check if the password matches the one stored in the database
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Create authentication using the provided credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signinRequest.getEmail(), signinRequest.getPassword()));

        // Generate JWT token for the authenticated user
        String jwt = jwtService.generateToken(user); // Pass user to JWT service

        // Prepare extra claims for the refresh token, can include user details or token expiration
        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("username", user.getUsername()); // Example additional claim

        // Optionally generate a refresh token (use the appropriate method)
        String refreshToken = jwtService.generateRefreshToken(extraClaims, user); // Pass user and extra claims

        // Prepare the response with JWT and refresh token
        JWTAuthenticationResponse jwtAuthenticationResponse = new JWTAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);

        return jwtAuthenticationResponse;
    }

    public JWTAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        // Extraire l'email de l'utilisateur à partir du refresh token
        String userEmail = jwtService.extractUsername(refreshTokenRequest.getToken());  // Correct usage of jwtService
        
        // Trouver l'utilisateur dans la base de données
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        
        // Vérifier si le token est valide
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) { // Correct usage of jwtService
            // Si le token est valide, générer un nouveau token
            String jwt = jwtService.generateToken(user);
            
            // Créer la réponse avec le nouveau jeton JWT et le refresh token
            JWTAuthenticationResponse jwtAuthenticationResponse = new JWTAuthenticationResponse();
            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken()); // Correct assignment of refresh token
            
            return jwtAuthenticationResponse;
        } else {
            // Si le refresh token n'est pas valide
            throw new RuntimeException("Invalid refresh token");
        }
    }
   
  
}
