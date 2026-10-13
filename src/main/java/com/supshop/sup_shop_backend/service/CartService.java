package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.cart.CartItemResponse;
import com.supshop.sup_shop_backend.model.Cart;
import com.supshop.sup_shop_backend.model.CartItem;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(String username) {
        User user = getUserByUsername(username);
        return cartRepository.findByUserId(user.getId())
                .map(cart -> cartItemRepository.findByCartId(cart.getId())
                        .stream()
                        .map(this::toResponse)
                        .toList())
                .orElse(List.of());
    }

    @Transactional
    public CartItemResponse addItem(String username,
                                    Long productId,
                                    Integer quantity) {
        User user = getUserByUsername(username);
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        "Товар не найден: " + productId));

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new IllegalArgumentException(
                    "Товар недоступен для заказа: " + product.getName());
        }

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Недостаточно товара на складе. Доступно: "
                            + product.getQuantity());
        }

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        return toResponse(cartItemRepository.save(item));
    }

    @Transactional
    public void removeItem(String username, Long productId) {
        User user = getUserByUsername(username);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Корзина не найдена"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException(
                        "Товар не найден в корзине"));

        cartItemRepository.delete(item);
    }

    @Transactional
    public CartItemResponse updateQuantity(String username,
                                           Long productId,
                                           Integer quantity) {
        User user = getUserByUsername(username);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Корзина не найдена"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException(
                        "Товар не найден в корзине"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        Product product = productRepository
                .findById(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException(
                        "Товар не найден"));

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Недостаточно товара на складе. Доступно: "
                            + product.getQuantity());
        }

        item.setQuantity(quantity);
        return toResponse(cartItemRepository.save(item));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден: " + username));
    }

    private CartItemResponse toResponse(CartItem item) {
        Product p = productRepository
                .findById(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
        return new CartItemResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                item.getQuantity(),
                p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}