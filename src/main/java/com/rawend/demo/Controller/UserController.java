package com.rawend.demo.Controller;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.dto.UserUpdateRequest;
import com.rawend.demo.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/user")
@RequiredArgsConstructor
public class UserController {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	 @GetMapping
	    public ResponseEntity<String> sayHi() {
	        
	        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	        
	        
	        String username = userDetails.getUsername();

	        
	        return ResponseEntity.ok("Hi " + username + ", please");
	    }
	 @PutMapping("/update-profile")
	 public ResponseEntity<String> updateProfile(@RequestBody UserUpdateRequest userUpdateRequest) {
	    
	     String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
	     Optional<User> userOpt = userRepository.findByEmail(currentEmail); // Recherche par email, car il est unique

	     if (!userOpt.isPresent()) {
	         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	     }

	     User user = userOpt.get();

	     
	     if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().isEmpty()) {
	         user.setUsername(userUpdateRequest.getUsername());
	     }

	     if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().equals(user.getEmail())) {
	         user.setEmail(userUpdateRequest.getEmail());
	     }

	     if (userUpdateRequest.getPhone() != null) {
	         user.setPhone(userUpdateRequest.getPhone());
	     }

	     if (userUpdateRequest.getOldPassword() != null && userUpdateRequest.getNewPassword() != null) {
	         if (!passwordEncoder.matches(userUpdateRequest.getOldPassword(), user.getPassword())) {
	             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
	         }
	         user.setPassword(passwordEncoder.encode(userUpdateRequest.getNewPassword()));
	     }

	     userRepository.save(user);
	     return ResponseEntity.ok("Profile updated successfully");
	 }
	 @GetMapping("/profile")
	 public ResponseEntity<?> getUserProfile() {
	     String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
	     Optional<User> userOpt = userRepository.findByEmail(currentEmail);

	     if (!userOpt.isPresent()) {
	         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	     }

	     User user = userOpt.get();

	    
	     Map<String, Object> userProfile = new HashMap<>();
	     userProfile.put("username", user.getUsernameFieldDirectly());
	     userProfile.put("email", user.getEmail());
	     userProfile.put("phone", user.getPhone());

	     return ResponseEntity.ok(userProfile);
	 }


}
