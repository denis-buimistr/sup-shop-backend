package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.UserResponse;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        User user = getUser(username);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String username,
                                      String fullName,
                                      String email) {
        User user = getUser(username);

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new BadRequestException("error.auth.emailTaken");
            }
            user.setEmail(email);
        }

        if (fullName != null) {
            user.setFullName(fullName);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String username,
                               String oldPassword,
                               String newPassword) {
        User user = getUser(username);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("error.auth.wrongPassword");
        }


        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("error.user.notFound"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                user.getRoles().stream().map(r -> r.getName()).toList(),
                user.getCreatedAt()
        );
    }
}