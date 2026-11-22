package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.auth.AuthResponse;
import com.supshop.sup_shop_backend.dto.auth.ForgotPasswordRequest;
import com.supshop.sup_shop_backend.dto.auth.LoginRequest;
import com.supshop.sup_shop_backend.dto.auth.RegisterRequest;
import com.supshop.sup_shop_backend.dto.auth.ResetPasswordRequest;
import com.supshop.sup_shop_backend.service.AuthService;
import com.supshop.sup_shop_backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (
           @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid  @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // всегда 204, даже если такой почты нет, иначе по ответу видно, кто у нас зарегистрирован
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
