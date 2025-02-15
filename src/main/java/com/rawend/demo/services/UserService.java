package com.rawend.demo.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import com.rawend.demo.entity.User;
import java.util.Optional;

public interface UserService {
    UserDetailsService userDetailsService();
    User save(User user);
    Optional<User> findById(Long id);
    void delete(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);  // Ajoute cette méthode
    Optional<User> findByPhone(String phone); // Ajoute cette ligne
   
}
