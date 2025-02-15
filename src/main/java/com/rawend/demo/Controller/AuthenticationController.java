package com.rawend.demo.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.dto.JWTAuthenticationResponse;
import com.rawend.demo.dto.RefreshTokenRequest;
import com.rawend.demo.dto.SignUpRequest;
import com.rawend.demo.dto.SigninRequest;
import com.rawend.demo.services.impl.AuthenticationServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequest signupRequest) {
        try {
        	authService.signup(signupRequest);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/signin")
    public ResponseEntity<JWTAuthenticationResponse> signin(@RequestBody SigninRequest signinRequest) {
        try {
            JWTAuthenticationResponse response = authService.signin(signinRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JWTAuthenticationResponse());
        }
    }@PostMapping("/refresh")
    public ResponseEntity<JWTAuthenticationResponse> refresh(@RequestBody RefreshTokenRequest RefreshRequest) {


        // Return the JWT tokens (access token and refresh token) in the response
        return ResponseEntity.ok(authService.refreshToken(RefreshRequest));
    }
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String phone) {
        // Vérifier si le téléphone existe dans la base de données
        boolean phoneExists = userRepository.existsByPhone(phone);

        Map<String, Object> response = new HashMap<>();
        response.put("phoneExists", phoneExists);

        if (phoneExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Téléphone existe déjà
        } else {
            return ResponseEntity.ok(response); // Téléphone n'existe pas
        }
    }
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        // Vérifier si l'email existe dans la base de données
        boolean emailExists = userRepository.existsByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("emailExists", emailExists);

        if (emailExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Email existe déjà
        } else {
            return ResponseEntity.ok(response); // Email n'existe pas
        }
    }
    }

