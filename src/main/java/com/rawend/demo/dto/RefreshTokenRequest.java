package com.rawend.demo.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
private String Token;

public void setToken(String token) {
	Token = token;
}

public String getToken() {
	return Token;
}
}
