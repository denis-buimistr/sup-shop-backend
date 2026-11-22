package com.supshop.sup_shop_backend.integration;

import com.supshop.sup_shop_backend.dto.auth.LoginRequest;
import com.supshop.sup_shop_backend.dto.auth.RegisterRequest;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.service.AuthService;
import com.supshop.sup_shop_backend.service.PasswordResetService;
import com.supshop.sup_shop_backend.service.email.EmailMessage;
import com.supshop.sup_shop_backend.service.email.EmailSender;
import com.supshop.sup_shop_backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Locale;
import java.util.UUID;

import static com.supshop.sup_shop_backend.support.AppExceptionAssert.assertKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

// весь сброс на настоящем Postgres: токен в базе, смена пароля, вход. подменяю только отправку письма,
// чтобы тестам не нужен был почтовый сервер
class PasswordResetIntegrationTest extends AbstractIntegrationTest {

    private static final String OLD_PASSWORD = "old-pass-2026";
    private static final String NEW_PASSWORD = "new-pass-2026";

    @MockitoBean private EmailSender emailSender;
    @Autowired private PasswordResetService passwordResetService;
    @Autowired private AuthService authService;
    @Autowired private JdbcTemplate jdbc;

    private String username;
    private String email;

    @BeforeEach
    void registerUser() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        String id = UUID.randomUUID().toString().substring(0, 8);
        username = "reset." + id;
        email = "reset." + id + "@mail.md";
        authService.register(new RegisterRequest(username, email, OLD_PASSWORD, null));
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void userCanSetNewPasswordWithLinkFromEmail() {
        passwordResetService.requestReset(email);
        String token = tokenFromEmail();

        passwordResetService.resetPassword(token, NEW_PASSWORD);

        assertThat(authService.login(new LoginRequest(email, NEW_PASSWORD)).username()).isEqualTo(username);
        assertThatThrownBy(() -> authService.login(new LoginRequest(username, OLD_PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void databaseKeepsHashNotTheTokenItself() {
        passwordResetService.requestReset(email);
        String token = tokenFromEmail();

        String stored = jdbc.queryForObject("""
                select t.token_hash from password_reset_tokens t
                join users u on u.id = t.user_id where u.username = ?
                """, String.class, username);

        assertThat(stored).hasSize(64).isNotEqualTo(token);
    }

    @Test
    void sameLinkWorksOnlyOnce() {
        passwordResetService.requestReset(email);
        String token = tokenFromEmail();
        passwordResetService.resetPassword(token, NEW_PASSWORD);

        assertKey(() -> passwordResetService.resetPassword(token, "third-pass-2026"),
                BadRequestException.class, "error.passwordReset.invalidToken");
    }

    // ждать 30 минут в тесте не буду, просто сдвигаю срок в базе в прошлое
    @Test
    void expiredLinkIsRejected() {
        passwordResetService.requestReset(email);
        String token = tokenFromEmail();
        jdbc.update("""
                update password_reset_tokens set expires_at = now() - interval '1 minute'
                where user_id = (select id from users where username = ?)
                """, username);

        assertKey(() -> passwordResetService.resetPassword(token, NEW_PASSWORD),
                BadRequestException.class, "error.passwordReset.invalidToken");
    }

    @Test
    void newRequestKillsOlderLink() {
        passwordResetService.requestReset(email);
        String older = tokenFromEmail();
        clearInvocations(emailSender);
        passwordResetService.requestReset(email);
        String newer = tokenFromEmail();

        assertKey(() -> passwordResetService.resetPassword(older, NEW_PASSWORD),
                BadRequestException.class, "error.passwordReset.invalidToken");
        passwordResetService.resetPassword(newer, NEW_PASSWORD);
    }

    private String tokenFromEmail() {
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo(email);
        return sent.getValue().text().replaceAll("(?s).*token=([A-Za-z0-9_-]+).*", "$1");
    }
}
