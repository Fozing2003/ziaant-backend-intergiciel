package com.ziaant.auth_service.repository;

import com.ziaant.auth_service.model.StatutCompte;
import com.ziaant.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatut(StatutCompte statut);
}
