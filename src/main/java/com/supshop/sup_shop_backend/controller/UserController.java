package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.ChangePasswordRequest;
import com.supshop.sup_shop_backend.dto.UpdateProfileRequest;
import com.supshop.sup_shop_backend.dto.UserResponse;
import com.supshop.sup_shop_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                userService.updateProfile(
                        userDetails.getUsername(), request.fullName(), request.email()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(
                userDetails.getUsername(),
                request.oldPassword(),
                request.newPassword());
        return ResponseEntity.noContent().build();
    }
}