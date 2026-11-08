package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.wishlist.WishlistResponse;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import com.supshop.sup_shop_backend.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistItemRepository wishlistItemRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public WishlistResponse get(String username) {
        return new WishlistResponse(wishlistItemRepository.findProductIdsByUserId(getUser(username).getId()));
    }

    // повторное добавление ничего не делает, поэтому фронт может спокойно жать сердечко дважды
    @Transactional
    public void add(String username, Long productId) {
        User user = getUser(username);
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
        wishlistItemRepository.addAll(user.getId(), List.of(productId));
    }

    @Transactional
    public void remove(String username, Long productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(getUser(username).getId(), productId);
    }

    // после входа переношу избранное гостя в аккаунт. товары, которых уже нет в базе, просто пропускаю
    @Transactional
    public WishlistResponse merge(String username, List<Long> productIds) {
        User user = getUser(username);
        if (!productIds.isEmpty()) {
            wishlistItemRepository.addAll(user.getId(), productIds);
        }
        return new WishlistResponse(wishlistItemRepository.findProductIdsByUserId(user.getId()));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }
}
