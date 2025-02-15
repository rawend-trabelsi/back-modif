package com.rawend.demo.services.impl;

import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.entity.User;
import com.rawend.demo.services.UserService;
import com.rawend.demo.utils.EmailService;
import com.rawend.demo.utils.PasswordUtils;

import lombok.RequiredArgsConstructor;

@Service

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final  EmailService emailService; 
    

    public UserServiceImpl(UserRepository userRepository,@Lazy PasswordEncoder passwordEncoder, EmailService emailService) {
	
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	// Utilisation de @RequiredArgsConstructor pour l'injection automatique

    public User save(User user) {
        // Générer un mot de passe temporaire
        String tempPassword = PasswordUtils.generateRandomPassword(10);
        
        // Hacher le mot de passe avant de le sauvegarder
        user.setPassword(passwordEncoder.encode(tempPassword));

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Envoyer le mot de passe temporaire par e-mail
        emailService.sendWelcomeEmail(savedUser, tempPassword);

        return savedUser;
    }

    // Returns a custom UserDetailsService
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // Find user by email
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
                
                // Return the user as a UserDetails object
                return user;
            }
        };
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UsernameNotFoundException("User not found with ID: " + id);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
