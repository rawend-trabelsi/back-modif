// PasswordResetController.java
package com.rawend.demo.Controller;

import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.entity.User;
import com.rawend.demo.services.PasswordResetService;
import com.rawend.demo.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    private final EmailService emailService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            logger.warn("L'adresse e-mail est requise mais manquante.");
            return ResponseEntity.badRequest().body("L'adresse e-mail est requise.");
        }

        String token = passwordResetService.generateResetToken(email);
        emailService.sendVerificationCode(email, token);

        logger.info("Code de vérification envoyé à l'adresse : {}", email);
        return ResponseEntity.ok("Code de vérification envoyé par e-mail.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String token = request.get("token");

        if (email == null || email.isBlank() || token == null || token.isBlank()) {
            logger.warn("Adresse e-mail ou code de réinitialisation manquant.");
            return ResponseEntity.badRequest().body("Adresse e-mail et code de réinitialisation requis.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("Utilisateur non trouvé pour l'adresse e-mail : {}", email);
            return ResponseEntity.status(404).body("Utilisateur non trouvé.");
        }

        User user = userOptional.get();
        String storedCode = user.getVerificationCode();

        if (storedCode == null || storedCode.isEmpty() || !storedCode.equals(token)) {
            logger.warn("Code de vérification invalide ou expiré pour l'utilisateur : {}", email);
            return ResponseEntity.status(400).body("Code de vérification invalide ou expiré.");
        }

        passwordResetService.invalidateToken(email);
        logger.info("Code de vérification validé pour l'utilisateur : {}", email);
        return ResponseEntity.ok("Code vérifié avec succès. Vous pouvez réinitialiser votre mot de passe.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || email.isBlank() || newPassword == null || newPassword.isBlank()) {
            logger.warn("Adresse e-mail ou nouveau mot de passe manquant.");
            return ResponseEntity.badRequest().body("Adresse e-mail et nouveau mot de passe requis.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("Utilisateur non trouvé pour la réinitialisation du mot de passe : {}", email);
            return ResponseEntity.badRequest().body("Utilisateur non trouvé.");
        }

        User user = userOptional.get();

        // Vérifier si le nouveau mot de passe est identique à l'ancien
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("Le nouveau mot de passe ne peut pas être identique à l'ancien pour l'utilisateur : {}", email);
            return ResponseEntity.badRequest().body("Le nouveau mot de passe ne peut pas être identique à l'ancien.");
        }

        try {
            passwordResetService.updatePassword(user, newPassword);
            logger.info("Mot de passe réinitialisé avec succès pour l'utilisateur : {}", email);
            return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
        } catch (IllegalArgumentException e) {
            logger.error("Erreur lors de la réinitialisation du mot de passe : {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/resend-verification-code")
    public ResponseEntity<String> resendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            logger.warn("L'adresse e-mail est requise mais manquante.");
            return ResponseEntity.badRequest().body("L'adresse e-mail est requise.");
        }

        // Générer un nouveau code de vérification
        String token = passwordResetService.generateResetToken(email);

        // Envoyer le code à l'email de l'utilisateur
        emailService.sendVerificationCode(email, token);

        logger.info("Nouveau code de vérification envoyé à l'adresse : {}", email);
        return ResponseEntity.ok("Nouveau code de vérification envoyé par e-mail.");
    }


}
