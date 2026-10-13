package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.UserResponse;
import com.supshop.sup_shop_backend.model.Order;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.UserRepository;
import com.supshop.sup_shop_backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public AdminController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrder());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getFullName(),
                        u.isEnabled(),
                        u.getRoles().stream()
                                .map(r -> r.getName())
                                .toList(),
                        u.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(users);
    }
}
