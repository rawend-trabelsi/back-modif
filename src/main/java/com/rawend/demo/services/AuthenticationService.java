package com.rawend.demo.services;

import com.rawend.demo.dto.JWTAuthenticationResponse;
import com.rawend.demo.dto.RefreshTokenRequest;
import com.rawend.demo.dto.SignUpRequest;
import com.rawend.demo.dto.SigninRequest;
import com.rawend.demo.entity.User;

public interface AuthenticationService {
	User signup(SignUpRequest signupRequest);
	 JWTAuthenticationResponse signin(SigninRequest signinRequest);
	 JWTAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
