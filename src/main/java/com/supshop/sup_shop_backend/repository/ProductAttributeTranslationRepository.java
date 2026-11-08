package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.ProductAttributeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProductAttributeTranslationRepository extends JpaRepository<ProductAttributeTranslation, ProductAttributeTranslation.Key> {

    // у перевода нет product_id, поэтому иду через саму характеристику. одним запросом на всю страницу каталога
    @Query("""
            select t from ProductAttributeTranslation t
            where t.lang = :lang
              and t.attributeId in (select a.id from ProductAttribute a where a.product.id in :productIds)
            """)
    List<ProductAttributeTranslation> findForProducts(String lang, Collection<Long> productIds);
}
