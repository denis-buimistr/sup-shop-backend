package com.supshop.sup_shop_backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // строки вставляет WishlistItemRepository.addAll одним SQL, created_at ставит DEFAULT в БД,
    // поэтому тут только чтение
    protected WishlistItem() {
    }

    public Long getId() {return id;}
    public User getUser() {return user;}
    public Product getProduct() {return product;}
    public LocalDateTime getCreatedAt() {return createdAt;}
}
