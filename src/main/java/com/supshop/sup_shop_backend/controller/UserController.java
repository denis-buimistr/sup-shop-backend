package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.UserResponse;
import com.supshop.sup_shop_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                userService.updateProfile(
                        userDetails.getUsername(),
                        body.get("fullName"),
                        body.get("email")));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        userService.changePassword(
                userDetails.getUsername(),
                body.get("oldPassword"),
                body.get("newPassword"));
        return ResponseEntity.noContent().build();
    }
}