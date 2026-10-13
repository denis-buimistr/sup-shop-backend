package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.model.Order;
import com.supshop.sup_shop_backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String shippingAddress = body.get("shippingAddress");
        return ResponseEntity.ok(
                orderService.checkout(
                        userDetails.getUsername(), shippingAddress));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Order>> myOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                orderService.getMyOrders(userDetails.getUsername()));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        Order.Status newStatus = Order.Status
                .valueOf(body.get("status"));
        return ResponseEntity.ok(
                orderService.updateStatus(orderId, newStatus));
    }
}