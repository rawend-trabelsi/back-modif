package com.rawend.demo.dto;

import lombok.Data;



@Data
public class SignUpRequest{
    private String username;
    private String email;
    private String password; // Include only during signup
    private String phone;
    private String confirmPassword; 
}
