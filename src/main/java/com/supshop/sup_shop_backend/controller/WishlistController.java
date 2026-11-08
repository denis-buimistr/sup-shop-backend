package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.wishlist.WishlistMergeRequest;
import com.supshop.sup_shop_backend.dto.wishlist.WishlistResponse;
import com.supshop.sup_shop_backend.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// избранное только для вошедших, гость хранит его в браузере и присылает в /merge после входа
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public WishlistResponse get(@AuthenticationPrincipal UserDetails user) {
        return wishlistService.get(user.getUsername());
    }

    // PUT, а не POST: «пусть товар будет в избранном», повтор даёт тот же результат
    @PutMapping("/{productId}")
    public ResponseEntity<Void> add(@AuthenticationPrincipal UserDetails user, @PathVariable Long productId) {
        wishlistService.add(user.getUsername(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserDetails user, @PathVariable Long productId) {
        wishlistService.remove(user.getUsername(), productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public WishlistResponse merge(@AuthenticationPrincipal UserDetails user,
                                  @Valid @RequestBody WishlistMergeRequest request) {
        return wishlistService.merge(user.getUsername(), request.productIds());
    }
}
