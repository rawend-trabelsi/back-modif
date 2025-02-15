package com.rawend.demo.Repository;

import com.rawend.demo.entity.Role;
import com.rawend.demo.entity.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByEmail(String email);
   Optional<User> findByPhone(String phone); // ✅ Correction ici
  
   boolean existsByEmail(String email);
   boolean existsByPhone(String phone);
   List<User> findByRole(Role role);

 
   boolean existsByUsername(String username);
Optional<User> findByUsername(String username);
  Optional<User> findById(Long id);  // Cette méthode doit exister
}
