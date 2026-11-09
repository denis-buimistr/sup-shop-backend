package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.wishlist.WishlistResponse;
import com.supshop.sup_shop_backend.service.WishlistService;
import com.supshop.sup_shop_backend.support.WithAppSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
@WithAppSecurity
class WishlistControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WishlistService wishlistService;

    @Test
    void wishlistNeedsSignIn() throws Exception {
        mockMvc.perform(get("/api/wishlist")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/wishlist/5")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ivan")
    void returnsProductIds() throws Exception {
        when(wishlistService.get("ivan")).thenReturn(new WishlistResponse(List.of(9L, 3L)));

        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productIds[0]").value(9))
                .andExpect(jsonPath("$.productIds[1]").value(3));
    }

    @Test
    @WithMockUser(username = "ivan")
    void addAndRemoveAnswer204() throws Exception {
        mockMvc.perform(put("/api/wishlist/5")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/wishlist/5")).andExpect(status().isNoContent());

        verify(wishlistService).add("ivan", 5L);
        verify(wishlistService).remove("ivan", 5L);
    }

    // гость не может прислать бесконечный список: больше 200 id отсекаю валидацией
    @Test
    @WithMockUser(username = "ivan")
    void mergeOfTooManyIdsIs400() throws Exception {
        String ids = LongStream.rangeClosed(1, 201).mapToObj(String::valueOf).collect(Collectors.joining(","));

        mockMvc.perform(post("/api/wishlist/merge").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productIds\": [" + ids + "]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wishlist can hold at most 200 products"));
        verify(wishlistService, never()).merge(anyString(), any());
    }

    @Test
    @WithMockUser(username = "ivan")
    void mergeWithoutListIs400() throws Exception {
        mockMvc.perform(post("/api/wishlist/merge").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Send the product ids to merge"));
    }
}
