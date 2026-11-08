package com.supshop.sup_shop_backend.dto.wishlist;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// то, что гость накопил в браузере до входа
public record WishlistMergeRequest(
        @NotNull(message = "{wishlist.productIds.required}")
        @Size(max = 200, message = "{wishlist.productIds.size}")
        List<@NotNull Long> productIds
) {}
