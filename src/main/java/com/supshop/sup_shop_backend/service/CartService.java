package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.cart.CartItemResponse;
import com.supshop.sup_shop_backend.model.Cart;
import com.supshop.sup_shop_backend.model.CartItem;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.*;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
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

        if (quantity < 1) {
            throw new BadRequestException("error.cart.quantityPositive");
        }

        User user = getUserByUsername(username);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);


        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("error.product.notFound"));


        if (!"ACTIVE".equals(product.getStatus())) {
            throw new BadRequestException("error.product.unavailable", product.getName());
        }

        int alreadyInCart = (item == null) ? 0 : item.getQuantity();

        if (product.getQuantity() < alreadyInCart + quantity) {
            throw new BadRequestException("error.stock.notEnoughInCart", product.getQuantity(), alreadyInCart);

        }


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
                .orElseThrow(() -> new NotFoundException("error.cart.notFound"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("error.cart.itemNotFound"));

        cartItemRepository.delete(item);
    }

    @Transactional
    public CartItemResponse updateQuantity(String username,
                                           Long productId,
                                           Integer quantity) {
        User user = getUserByUsername(username);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("error.cart.notFound"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("error.cart.itemNotFound"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        Product product = productRepository
                .findById(item.getProduct().getId())
                .orElseThrow(() -> new NotFoundException("error.product.notFound"));

        if (product.getQuantity() < quantity) {
            throw new BadRequestException("error.stock.available", product.getQuantity());
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
                .orElseThrow(() -> new NotFoundException("error.user.notFound"));
    }

    private CartItemResponse toResponse(CartItem item) {
        Product p = productRepository
                .findById(item.getProduct().getId())
                .orElseThrow(() -> new NotFoundException("error.product.notFound"));
        return new CartItemResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                item.getQuantity(),
                p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}