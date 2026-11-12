package com.supshop.sup_shop_backend.dto;

import com.supshop.sup_shop_backend.dto.auth.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// на входе логин и почту различаю по @, поэтому в логине её быть не должно
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ivan", "ivan.petrov", "ivan_p-2026"})
    void usernameWithLettersDigitsDotDashUnderscoreIsValid(String username) {
        assertThat(usernameErrors(username)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ivan@mail.md", "ivan petrov", "иван"})
    void usernameWithAtSpaceOrCyrillicIsRejected(String username) {
        assertThat(usernameErrors(username)).isNotEmpty();
    }

    private Set<ConstraintViolation<RegisterRequest>> usernameErrors(String username) {
        RegisterRequest request = new RegisterRequest(username, "ivan@mail.md", "paddle-2026", null);
        return validator.validateProperty(request, "username");
    }
}
