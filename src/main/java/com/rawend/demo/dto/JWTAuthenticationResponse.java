package com.rawend.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JWTAuthenticationResponse {
    private String token;
    private String refreshToken = "";  // Valeur par défaut vide

    // Constructeur pour les cas où seul le token est nécessaire
    public JWTAuthenticationResponse(String token) {
        this.token = token;
        this.refreshToken = "";  // Définir refreshToken par défaut si non spécifié
    }
}
