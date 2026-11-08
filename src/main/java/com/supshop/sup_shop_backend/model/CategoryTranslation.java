package com.supshop.sup_shop_backend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

// название и описание категории на ro или ru, английский оригинал лежит в categories
@Entity
@Table(name = "category_translations")
@IdClass(CategoryTranslation.Key.class)
public class CategoryTranslation {

    @Id
    @Column(name = "category_id")
    private Long categoryId;

    @Id
    @Column(length = 2)
    private String lang;

    @Column(nullable = false, length = 150)
    private String name;

    private String description;

    protected CategoryTranslation() {
    }

    public CategoryTranslation(Long categoryId, String lang) {
        this.categoryId = categoryId;
        this.lang = lang;
    }

    public Long getCategoryId() {return categoryId;}
    public String getLang() {return lang;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    // составной ключ (category_id, lang)
    public static class Key implements Serializable {
        private Long categoryId;
        private String lang;

        @Override
        public boolean equals(Object o) {
            return o instanceof Key other && Objects.equals(categoryId, other.categoryId) && Objects.equals(lang, other.lang);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categoryId, lang);
        }
    }
}
