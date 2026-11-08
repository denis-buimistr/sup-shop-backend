package com.supshop.sup_shop_backend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

// характеристика на ro или ru. value пустой, если оригинал понятен и так (цифры, Carbon Reflex)
@Entity
@Table(name = "product_attribute_translations")
@IdClass(ProductAttributeTranslation.Key.class)
public class ProductAttributeTranslation {

    @Id
    @Column(name = "attribute_id")
    private Long attributeId;

    @Id
    @Column(length = 2)
    private String lang;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String value;

    protected ProductAttributeTranslation() {
    }

    public Long getAttributeId() {return attributeId;}
    public String getLang() {return lang;}
    public String getName() {return name;}
    public String getValue() {return value;}

    // составной ключ (attribute_id, lang)
    public static class Key implements Serializable {
        private Long attributeId;
        private String lang;

        @Override
        public boolean equals(Object o) {
            return o instanceof Key other && Objects.equals(attributeId, other.attributeId) && Objects.equals(lang, other.lang);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attributeId, lang);
        }
    }
}
