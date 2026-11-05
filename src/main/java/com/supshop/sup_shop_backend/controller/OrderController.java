package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.order.CheckoutRequest;
import com.supshop.sup_shop_backend.dto.order.OrderResponse;
import com.supshop.sup_shop_backend.dto.order.UpdateOrderStatusRequest;
import com.supshop.sup_shop_backend.model.Order;
import com.supshop.sup_shop_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(
                orderService.checkout(
                        userDetails.getUsername(), request.shippingAddress()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                orderService.getMyOrders(userDetails.getUsername()));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(
                orderService.updateStatus(orderId, request.status()));
    }
}