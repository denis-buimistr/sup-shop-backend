package com.supshop.sup_shop_backend.integration;

import com.supshop.sup_shop_backend.dto.auth.AuthResponse;
import com.supshop.sup_shop_backend.dto.auth.LoginRequest;
import com.supshop.sup_shop_backend.dto.auth.RegisterRequest;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.service.AuthService;
import com.supshop.sup_shop_backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.UUID;

import static com.supshop.sup_shop_backend.support.AppExceptionAssert.assertKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// вход через настоящий AuthenticationManager и базу: по логину, по почте в любом регистре
class LoginIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "paddle-2026";

    @Autowired private AuthService authService;

    private String username;
    private String email;

    @BeforeEach
    void registerUser() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        username = "ivan." + id;
        email = "ivan." + id + "@mail.md";
        // регистрируюсь с заглавными буквами в почте, как часто вводят на телефоне
        authService.register(new RegisterRequest(username, " Ivan." + id + "@Mail.MD ", PASSWORD, null));
    }

    @Test
    void emailIsSavedInLowerCase() {
        assertThat(userRepository.findByUsername(username)).get()
                .extracting(user -> user.getEmail())
                .isEqualTo(email);
    }

    @Test
    void canSignInWithUsername() {
        AuthResponse response = authService.login(new LoginRequest(username, PASSWORD));

        assertThat(response.username()).isEqualTo(username);
        assertThat(response.token()).isNotBlank();
    }

    // в ответе и в токене всё равно логин, по нему дальше работает JwtAuthFilter
    @Test
    void canSignInWithEmailInAnyCase() {
        AuthResponse response = authService.login(new LoginRequest(email.toUpperCase(), PASSWORD));

        assertThat(response.username()).isEqualTo(username);
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThatThrownBy(() -> authService.login(new LoginRequest(email, "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void sameEmailInOtherCaseIsTaken() {
        RegisterRequest twin = new RegisterRequest(username + "-twin", email.toUpperCase(), PASSWORD, null);

        assertKey(() -> authService.register(twin), BadRequestException.class, "error.auth.emailTaken");
    }
}
