package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.ProductTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductTranslationRepository extends JpaRepository<ProductTranslation, ProductTranslation.Key> {

    List<ProductTranslation> findByLangAndProductIdIn(String lang, Collection<Long> productIds);

    List<ProductTranslation> findByProductIdOrderByLang(Long productId);

    Optional<ProductTranslation> findByProductIdAndLang(Long productId, String lang);
}
