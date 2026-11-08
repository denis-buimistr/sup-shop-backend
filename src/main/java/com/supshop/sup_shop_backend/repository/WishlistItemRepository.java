package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    // фронту нужны только id товаров, сами товары он берёт из каталога
    @Query("select w.product.id from WishlistItem w where w.user.id = :userId order by w.createdAt desc")
    List<Long> findProductIdsByUserId(Long userId);

    // сначала делал «проверил, что нет, потом вставил», но два запроса разом оба проходили проверку и падали на UNIQUE.
    // ON CONFLICT отдаёт это Postgres, а select из products заодно выкидывает id несуществующих товаров
    @Modifying
    @Query(value = """
            INSERT INTO wishlist_items (user_id, product_id)
            SELECT :userId, p.id FROM products p WHERE p.id IN (:productIds)
            ON CONFLICT (user_id, product_id) DO NOTHING
            """, nativeQuery = true)
    int addAll(Long userId, Collection<Long> productIds);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
