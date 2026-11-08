package com.supshop.sup_shop_backend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

// описание товара на ro или ru. name пустой почти всегда: название модели не переводится
@Entity
@Table(name = "product_translations")
@IdClass(ProductTranslation.Key.class)
public class ProductTranslation {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Id
    @Column(length = 2)
    private String lang;

    @Column(length = 200)
    private String name;

    private String description;

    protected ProductTranslation() {
    }

    public ProductTranslation(Long productId, String lang) {
        this.productId = productId;
        this.lang = lang;
    }

    public Long getProductId() {return productId;}
    public String getLang() {return lang;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    // составной ключ (product_id, lang)
    public static class Key implements Serializable {
        private Long productId;
        private String lang;

        @Override
        public boolean equals(Object o) {
            return o instanceof Key other && Objects.equals(productId, other.productId) && Objects.equals(lang, other.lang);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, lang);
        }
    }
}
