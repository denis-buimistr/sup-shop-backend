package com.supshop.sup_shop_backend.support;

import com.supshop.sup_shop_backend.config.SecurityConfig;
import com.supshop.sup_shop_backend.config.WebConfig;
import com.supshop.sup_shop_backend.security.CustomUserDetailsService;
import com.supshop.sup_shop_backend.security.JwtService;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @WebMvcTest сам не грузит мои @Configuration, а без SecurityConfig правила доступа были бы дефолтные.
// подключаю настоящие SecurityConfig и WebConfig, а то, что нужно JWT-фильтру, подменяю моками.
// пользователя в тестах подставляет @WithMockUser, поэтому токены тут не нужны
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SecurityConfig.class, WebConfig.class})
@MockitoBean(types = {JwtService.class, CustomUserDetailsService.class})
public @interface WithAppSecurity {
}
