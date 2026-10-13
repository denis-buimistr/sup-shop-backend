package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.cart.CartItemResponse;
import com.supshop.sup_shop_backend.model.CartItem;
import com.supshop.sup_shop_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(
                cartService.addItem(
                        userDetails.getUsername(), productId, quantity));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        CartItemResponse updated = cartService.updateQuantity(
                userDetails.getUsername(), productId, quantity);
        if (updated == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        cartService.removeItem(userDetails.getUsername(), productId);
        return ResponseEntity.noContent().build();
    }
}


