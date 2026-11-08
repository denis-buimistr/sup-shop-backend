package com.supshop.sup_shop_backend.dto.wishlist;

import java.util.List;

// id товаров в избранном, новые первыми
public record WishlistResponse(List<Long> productIds) {}
