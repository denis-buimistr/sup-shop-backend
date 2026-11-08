package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.CategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, CategoryTranslation.Key> {

    List<CategoryTranslation> findByLang(String lang);

    List<CategoryTranslation> findByCategoryIdOrderByLang(Long categoryId);

    Optional<CategoryTranslation> findByCategoryIdAndLang(Long categoryId, String lang);
}
