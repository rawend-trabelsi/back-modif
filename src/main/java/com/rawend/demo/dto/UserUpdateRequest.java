package com.rawend.demo.dto;


import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String email;
    private String phone;
    private String role;
    private String oldPassword;
    private String newPassword;
}
