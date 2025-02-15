package com.rawend.demo.services;

import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public PasswordResetService(UserRepository userRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    // Génère un code de réinitialisation et l'envoie par email
    public String generateResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé pour l'email : " + email);
        }

        String token = String.format("%06d", new Random().nextInt(999999));
        User user = userOptional.get();
        user.setVerificationCode(token);
        userRepository.save(user);

        sendVerificationEmail(email, token);
        return token;
    }

    // Envoie l'email de vérification
    private void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Code de vérification de réinitialisation de mot de passe");
        message.setText("Votre code de réinitialisation est : " + token);
        mailSender.send(message);
    }

    // Valide le code de réinitialisation
    public boolean validateResetToken(String email, String token) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(user -> token.equals(user.getVerificationCode())).orElse(false);
    }

    // Invalide le token après vérification
    public void invalidateToken(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setVerificationCode(null);
            userRepository.save(user);
        });
    }
    public void updatePassword(User user, String newPassword) {
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword)); // Hash du mot de passe
        userRepository.save(user);
    }

}
