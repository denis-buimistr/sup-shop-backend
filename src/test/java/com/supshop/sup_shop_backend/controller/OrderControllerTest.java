package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.order.CheckoutRequest;
import com.supshop.sup_shop_backend.dto.order.OrderResponse;
import com.supshop.sup_shop_backend.model.DeliveryMethod;
import com.supshop.sup_shop_backend.model.Order;
import com.supshop.sup_shop_backend.model.PaymentMethod;
import com.supshop.sup_shop_backend.service.OrderService;
import com.supshop.sup_shop_backend.support.WithAppSecurity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@WithAppSecurity
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private OrderService orderService;

    @Test
    void checkoutWithoutTokenIs401() throws Exception {
        mockMvc.perform(post("/api/orders/checkout").contentType(MediaType.APPLICATION_JSON).content(pickup()))
                .andExpect(status().isUnauthorized());
    }

    // заказ оформляется на того, кто вошёл, а не на имя из тела запроса
    @Test
    @WithMockUser(username = "ivan")
    void checkoutUsesSignedInUser() throws Exception {
        when(orderService.checkout(eq("ivan"), any())).thenReturn(order("NEW"));

        mockMvc.perform(post("/api/orders/checkout").contentType(MediaType.APPLICATION_JSON).content(pickup()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));

        ArgumentCaptor<CheckoutRequest> request = ArgumentCaptor.forClass(CheckoutRequest.class);
        verify(orderService).checkout(eq("ivan"), request.capture());
        assertThat(request.getValue().deliveryMethod()).isEqualTo(DeliveryMethod.PICKUP);
    }

    // курьеру нужен адрес: проверка в @AssertTrue на CheckoutRequest, а не в сервисе
    @Test
    @WithMockUser(username = "ivan")
    void courierWithoutAddressIs400() throws Exception {
        mockMvc.perform(post("/api/orders/checkout").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Ion", "lastName": "Munteanu", "phone": "+37369000000",
                                 "email": "ion@mail.md", "deliveryMethod": "COURIER_CHISINAU",
                                 "paymentMethod": "CASH_ON_DELIVERY"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City and address are required for this delivery method"));
        verify(orderService, never()).checkout(anyString(), any());
    }

    @Test
    @WithMockUser(username = "ivan")
    void unknownDeliveryMethodIs400() throws Exception {
        mockMvc.perform(post("/api/orders/checkout").contentType(MediaType.APPLICATION_JSON)
                        .content(pickup().replace("PICKUP", "DRONE")))
                .andExpect(status().isBadRequest());
        verify(orderService, never()).checkout(anyString(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void customerCannotChangeOrderStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/5/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"PAID\"}"))
                .andExpect(status().isForbidden());
        verify(orderService, never()).updateStatus(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminChangesOrderStatus() throws Exception {
        when(orderService.updateStatus(5L, Order.Status.PAID)).thenReturn(order("PAID"));

        mockMvc.perform(patch("/api/orders/5/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void missingStatusIs400InRussian() throws Exception {
        mockMvc.perform(patch("/api/orders/5/status").header("Accept-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Укажите статус"));
    }

    private static String pickup() {
        return """
                {"firstName": "Ion", "lastName": "Munteanu", "phone": "+37369000000",
                 "email": "ion@mail.md", "deliveryMethod": "PICKUP", "paymentMethod": "CASH_ON_DELIVERY"}
                """;
    }

    private static OrderResponse order(String status) {
        return new OrderResponse(5L, status, "ivan", "ivan@mail.md", "Ion", "Munteanu", "+37369000000",
                "ion@mail.md", DeliveryMethod.PICKUP, null, null, BigDecimal.ZERO, PaymentMethod.CASH_ON_DELIVERY,
                null, new BigDecimal("490"), List.of(), null);
    }
}
