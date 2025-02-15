package com.rawend.demo.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.dto.JWTAuthenticationResponse;
import com.rawend.demo.entity.Role;
import com.rawend.demo.entity.User;
import com.rawend.demo.services.impl.JWTServiceImpl;

@RestController
public class AuthController {

    @Autowired
    private JWTServiceImpl jwtService;

    @Autowired  
    private UserRepository userRepository;

    @CrossOrigin(origins = "*")
    @GetMapping("/home")
    public ResponseEntity<Map<String, String>> home(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, String> response = new HashMap<>();

        if (principal == null) {
            response.put("message", "Utilisateur non authentifié");
            return ResponseEntity.badRequest().body(response);
        }

        // Récupération des infos utilisateur
        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");

        if (email == null) {
            response.put("message", "Email introuvable");
            return ResponseEntity.badRequest().body(response);
        }

        // Vérification ou création de l'utilisateur
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(name);
            newUser.setEmail(email);
            userRepository.save(newUser);
            return newUser;
        });

        // Génération du JWT
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        response.put("message", "Authentification réussie");
        response.put("token", token);
        response.put("refreshToken", refreshToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/facebook")
    public ResponseEntity<?> authenticateWithFacebook(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        System.out.println("AccessToken: " + accessToken); // Log du token

        // Vérifier le token avec Facebook
        String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                // Convertir la réponse JSON en objet
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                String facebookId = jsonNode.get("id").asText();
                String name = jsonNode.get("name").asText();
                String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;

              
                System.out.println("Facebook ID: " + facebookId + ", Name: " + name + ", Email: " + email);

                User user = (email != null && !email.isEmpty()) ? 
                	    userRepository.findByEmail(email).orElseGet(() -> {
                	        User newUser = new User();
                	        newUser.setUsername(name);
                	        newUser.setEmail(email);
                	        newUser.setRole(Role.USER);  // Définir le rôle comme Role.USER
                	        userRepository.save(newUser);
                	        return newUser;
                	    }) : null;


                if (user == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("L'email est obligatoire pour authentifier l'utilisateur.");
                }

                // Log de l'utilisateur créé ou trouvé
                System.out.println("User: " + user.getUsername() + ", Email: " + user.getEmail());

                // Générer un JWT
                String token = jwtService.generateToken(user);
                return ResponseEntity.ok(new JWTAuthenticationResponse(token, null));

            } catch (Exception e) {
                // Log de l'erreur
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors du traitement des données Facebook.");
            }
        } else {
            // Log de la réponse échouée de Facebook
            System.out.println("Échec de la récupération des données Facebook, status: " + response.getStatusCode());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec de l'authentification Facebook.");
        }
    }

}
