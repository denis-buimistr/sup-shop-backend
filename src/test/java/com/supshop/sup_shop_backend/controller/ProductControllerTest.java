package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.ProductFilter;
import com.supshop.sup_shop_backend.dto.ProductRequest;
import com.supshop.sup_shop_backend.dto.ProductResponse;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.service.ProductService;
import com.supshop.sup_shop_backend.service.TranslationService;
import com.supshop.sup_shop_backend.support.WithAppSecurity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// поднимается только веб-слой: контроллер, GlobalExceptionHandler, валидация и security. сервисы — моки
@WebMvcTest(ProductController.class)
@WithAppSecurity
class ProductControllerTest {

    private static final String VALID_PRODUCT = """
            {"name": "Leash", "price": 490, "quantity": 3}
            """;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductService productService;
    @MockitoBean private TranslationService translationService;

    @Test
    void catalogIsPublicAndPassesFiltersToService() throws Exception {
        when(productService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(product()), Pageable.ofSize(20), 1));

        mockMvc.perform(get("/api/products").param("q", "carbon").param("brand", "1", "3").param("onSale", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Leash"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        // query string должен собраться в ProductFilter, иначе фильтры каталога молча перестанут работать
        ArgumentCaptor<ProductFilter> filter = ArgumentCaptor.forClass(ProductFilter.class);
        verify(productService).findAll(filter.capture(), any());
        assertThat(filter.getValue().q()).isEqualTo("carbon");
        assertThat(filter.getValue().brand()).containsExactly(1L, 3L);
        assertThat(filter.getValue().onSale()).isTrue();
    }

    @Test
    void notFoundComesAsErrorResponse() throws Exception {
        when(productService.findBySlug("nope")).thenThrow(new NotFoundException("error.product.notFound"));

        mockMvc.perform(get("/api/products/slug/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void errorTextFollowsAcceptLanguage() throws Exception {
        when(productService.findBySlug("nope")).thenThrow(new NotFoundException("error.product.notFound"));

        mockMvc.perform(get("/api/products/slug/nope").header("Accept-Language", "ru"))
                .andExpect(jsonPath("$.message").value("Товар не найден"));
        mockMvc.perform(get("/api/products/slug/nope").header("Accept-Language", "ro-MD,ro;q=0.9"))
                .andExpect(jsonPath("$.message").value("Produsul nu a fost găsit"));
    }

    @Test
    void creatingProductWithoutTokenIs401() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isUnauthorized());
        verify(productService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void creatingProductAsCustomerIs403() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isForbidden());
        verify(productService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreatesProduct() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(product());

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("leash"));
    }

    // все ошибки полей приходят одной строкой, а до сервиса запрос не доходит
    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidProductIs400WithAllFieldErrors() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "price": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Product name is required")))
                .andExpect(jsonPath("$.message").value(containsString("Price can not be negative")))
                .andExpect(jsonPath("$.message").value(containsString("Quantity is required")));
        verify(productService, never()).create(any());
    }

    // @AssertTrue на методе record тоже должен сработать и прийти по-румынски
    @Test
    @WithMockUser(roles = "ADMIN")
    void oldPriceLowerThanPriceIsRejectedInRequestLanguage() throws Exception {
        mockMvc.perform(post("/api/products").header("Accept-Language", "ro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Board", "price": 1000, "compareAtPrice": 900, "quantity": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Prețul vechi trebuie să fie mai mare decât cel actual"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void brokenJsonIs400NotServerError() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    private static ProductResponse product() {
        return new ProductResponse(1L, "Leash", "leash", null, new BigDecimal("490"), null, 3,
                "Starboard", "SUP Accessories", null, "ACTIVE", List.of(), List.of());
    }
}
