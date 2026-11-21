package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.model.PasswordResetToken;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.PasswordResetTokenRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import com.supshop.sup_shop_backend.util.ContentLanguage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
    private static final int MAX_REQUESTS_PER_HOUR = 3;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final MessageSource messageSource;
    private final String frontendUrl;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                MessageSource messageSource,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.messageSource = messageSource;
        this.frontendUrl = frontendUrl;
    }

    // если такой почты нет, молча выхожу. ответ одинаковый, чтобы по нему нельзя было узнать, кто у нас зарегистрирован
    @Transactional
    public void requestReset(String email) {
        Optional<User> found = userRepository.findByEmailIgnoreCase(email.trim());
        if (found.isEmpty()) {
            return;
        }
        User user = found.get();
        LocalDateTime now = LocalDateTime.now();
        if (tokenRepository.countByUserIdAndCreatedAtAfter(user.getId(), now.minusHours(1)) >= MAX_REQUESTS_PER_HOUR) {
            return;
        }

        tokenRepository.invalidateAll(user.getId(), now);
        String token = newToken();
        tokenRepository.save(new PasswordResetToken(user, hash(token), now.plus(TOKEN_LIFETIME)));
        sendResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hash(token))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new BadRequestException("error.passwordReset.invalidToken"));

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.markUsed(now);
        tokenRepository.invalidateAll(user.getId(), now);
    }

    private void sendResetEmail(String email, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        String lang = ContentLanguage.current();
        // английский на сайте без префикса, остальные языки под /ro и /ru
        String prefix = ContentLanguage.DEFAULT.equals(lang) ? "" : "/" + lang;
        String link = frontendUrl + prefix + "/reset-password?token=" + token;

        String subject = messageSource.getMessage("email.passwordReset.subject", null, locale);
        String body = messageSource.getMessage("email.passwordReset.body",
                new Object[]{link, TOKEN_LIFETIME.toMinutes()}, locale);
        emailService.send(email, subject, body);
    }

    // 32 случайных байта, это 256 бит, такой токен не подобрать перебором
    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}