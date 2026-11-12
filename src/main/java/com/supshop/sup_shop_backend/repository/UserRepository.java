package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // почту сравниваю без учёта регистра: Ivan@Mail.md и ivan@mail.md это один ящик
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRoles_Name(String roleName);
}
