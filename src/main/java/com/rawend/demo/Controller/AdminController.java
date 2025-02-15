package com.rawend.demo.Controller;

import com.rawend.demo.entity.User;

import com.rawend.demo.services.UserService;
import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.dto.UserUpdateRequest;
import com.rawend.demo.entity.Role;
import com.rawend.demo.utils.EmailService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final EmailService emailService;
     private final UserRepository userRepository;

     @PostMapping("/add-user")
     public ResponseEntity<String> addUser(@RequestBody User user) {
         if (user.getRole() == null || (!user.getRole().equals(Role.ADMIN) && !user.getRole().equals(Role.TECHNICIEN))) {
             return ResponseEntity.badRequest().body("Invalid role. Only ADMIN or TECHNICIEN allowed.");
         }

         User savedUser = userService.save(user);

         return ResponseEntity.ok("User created successfully with role: " + savedUser.getRole());
     }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            userService.delete(id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }
    @PutMapping("/update-user/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest userUpdateRequest) {
        Optional<User> existingUserOpt = userRepository.findById(id);

        if (!existingUserOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User existingUser = existingUserOpt.get();

        
        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().isEmpty()) {
            existingUser.setUsername(userUpdateRequest.getUsername());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().equals(existingUser.getEmail())) {
            existingUser.setEmail(userUpdateRequest.getEmail());
        }

        if (userUpdateRequest.getPhone() != null) {
            existingUser.setPhone(userUpdateRequest.getPhone());
        }

        if (userUpdateRequest.getRole() != null) {
            existingUser.setRole(Role.valueOf(userUpdateRequest.getRole()));  
        }

        userRepository.save(existingUser);

        return ResponseEntity.ok("User updated successfully  " );
    }
  
    @GetMapping("/users")
    public ResponseEntity<?> getAdminAndTechnicianUsers() {
        // Récupérer tous les utilisateurs avec les rôles ADMIN ou TECHNICIEN
        List<User> users = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.TECHNICIEN)
                .collect(Collectors.toList());

        // Construire une liste avec les informations nécessaires (username, email, phone, role)
        List<Map<String, Object>> response = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsernameFieldDirectly()); // Appel explicite au vrai champ username
                    userMap.put("email", user.getEmail());
                    userMap.put("phone", user.getPhone());
                    userMap.put("role", user.getRole().toString());
                    return userMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }



    @GetMapping
    public ResponseEntity<String> sayHi() {
        return ResponseEntity.ok("Hi admin");
    }
}