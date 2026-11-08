package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.TranslationRequest;
import com.supshop.sup_shop_backend.dto.TranslationResponse;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.CategoryTranslation;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.ProductAttributeTranslation;
import com.supshop.sup_shop_backend.model.ProductTranslation;
import com.supshop.sup_shop_backend.repository.CategoryRepository;
import com.supshop.sup_shop_backend.repository.CategoryTranslationRepository;
import com.supshop.sup_shop_backend.repository.ProductAttributeTranslationRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import com.supshop.sup_shop_backend.repository.ProductTranslationRepository;
import com.supshop.sup_shop_backend.util.ContentLanguage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// Переводы контента на язык из Accept-Language. для английского ничего не грузит, оригинал уже в сущностях
@Service
public class TranslationService {

    private final CategoryTranslationRepository categoryTranslationRepository;
    private final ProductTranslationRepository productTranslationRepository;
    private final ProductAttributeTranslationRepository productAttributeTranslationRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public TranslationService(CategoryTranslationRepository categoryTranslationRepository,
                              ProductTranslationRepository productTranslationRepository,
                              ProductAttributeTranslationRepository productAttributeTranslationRepository,
                              CategoryRepository categoryRepository,
                              ProductRepository productRepository) {
        this.categoryTranslationRepository = categoryTranslationRepository;
        this.productTranslationRepository = productTranslationRepository;
        this.productAttributeTranslationRepository = productAttributeTranslationRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // категорий всего несколько десятков, поэтому беру все переводы языка разом
    @Transactional(readOnly = true)
    public CategoryTexts categories() {
        String lang = ContentLanguage.current();
        if (lang.equals(ContentLanguage.DEFAULT)) {
            return CategoryTexts.ORIGINAL;
        }
        return new CategoryTexts(categoryTranslationRepository.findByLang(lang).stream()
                .collect(Collectors.toMap(CategoryTranslation::getCategoryId, Function.identity())));
    }

    // на страницу каталога три запроса на все товары сразу, а не по запросу на каждый товар
    @Transactional(readOnly = true)
    public ProductTexts products(Collection<Product> products) {
        String lang = ContentLanguage.current();
        if (lang.equals(ContentLanguage.DEFAULT) || products.isEmpty()) {
            return ProductTexts.ORIGINAL;
        }
        List<Long> ids = products.stream().map(Product::getId).toList();
        return new ProductTexts(
                productTranslationRepository.findByLangAndProductIdIn(lang, ids).stream()
                        .collect(Collectors.toMap(ProductTranslation::getProductId, Function.identity())),
                productAttributeTranslationRepository.findForProducts(lang, ids).stream()
                        .collect(Collectors.toMap(ProductAttributeTranslation::getAttributeId, Function.identity())),
                categories()
        );
    }

    // --- админка: переводы одной категории или товара

    @Transactional(readOnly = true)
    public List<TranslationResponse> forCategory(Long categoryId) {
        ensureCategoryExists(categoryId);
        return categoryTranslationRepository.findByCategoryIdOrderByLang(categoryId).stream()
                .map(t -> new TranslationResponse(t.getLang(), t.getName(), t.getDescription()))
                .toList();
    }

    // оба поля пустые — удаляю перевод, и на сайте снова будет английский
    @Transactional
    public List<TranslationResponse> saveForCategory(Long categoryId, String lang, TranslationRequest request) {
        ensureCategoryExists(categoryId);
        ensureTranslatedLang(lang);
        String name = blankToNull(request.name());
        String description = blankToNull(request.description());

        if (name == null && description == null) {
            categoryTranslationRepository.findByCategoryIdAndLang(categoryId, lang).ifPresent(categoryTranslationRepository::delete);
        } else {
            // у категории без названия перевод бессмысленный, в меню была бы пустая строка
            if (name == null) {
                throw new BadRequestException("error.translation.nameRequired");
            }
            CategoryTranslation t = categoryTranslationRepository.findByCategoryIdAndLang(categoryId, lang)
                    .orElseGet(() -> new CategoryTranslation(categoryId, lang));
            t.setName(name);
            t.setDescription(description);
            categoryTranslationRepository.save(t);
        }
        return forCategory(categoryId);
    }

    @Transactional(readOnly = true)
    public List<TranslationResponse> forProduct(Long productId) {
        ensureProductExists(productId);
        return productTranslationRepository.findByProductIdOrderByLang(productId).stream()
                .map(t -> new TranslationResponse(t.getLang(), t.getName(), t.getDescription()))
                .toList();
    }

    // у товара название обычно не переводится, поэтому тут обязательных полей нет
    @Transactional
    public List<TranslationResponse> saveForProduct(Long productId, String lang, TranslationRequest request) {
        ensureProductExists(productId);
        ensureTranslatedLang(lang);
        String name = blankToNull(request.name());
        String description = blankToNull(request.description());

        if (name == null && description == null) {
            productTranslationRepository.findByProductIdAndLang(productId, lang).ifPresent(productTranslationRepository::delete);
        } else {
            ProductTranslation t = productTranslationRepository.findByProductIdAndLang(productId, lang)
                    .orElseGet(() -> new ProductTranslation(productId, lang));
            t.setName(name);
            t.setDescription(description);
            productTranslationRepository.save(t);
        }
        return forProduct(productId);
    }

    private void ensureCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("error.category.notFound");
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("error.product.notFound");
        }
    }

    // английский живёт в основной таблице, переводить можно только на ro и ru
    private void ensureTranslatedLang(String lang) {
        if (!ContentLanguage.isTranslated(lang)) {
            throw new BadRequestException("error.translation.lang", lang);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
