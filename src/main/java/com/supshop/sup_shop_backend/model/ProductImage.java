package com.supshop.sup_shop_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}

    public Product getProduct() {return product;}
    public void setProduct(Product product) {this.product = product;}

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public boolean isPrimary() {return primary;}
    public void setPrimary(boolean primary) {this.primary = primary;}

    public Integer getSortOrder() {return sortOrder;}
    public void setSortOrder(Integer sortOrder) {this.sortOrder = sortOrder;}
}
