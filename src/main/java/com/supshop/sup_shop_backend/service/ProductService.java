package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.ProductAttributeResponse;
import com.supshop.sup_shop_backend.dto.ProductImageResponse;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.Brand;
import com.supshop.sup_shop_backend.model.Category;
import com.supshop.sup_shop_backend.dto.ProductFilter;
import com.supshop.sup_shop_backend.dto.ProductRequest;
import com.supshop.sup_shop_backend.dto.ProductResponse;
import com.supshop.sup_shop_backend.repository.*;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.supshop.sup_shop_backend.util.SlugUtil;

import java.util.ArrayList;
import java.util.List;

import static com.supshop.sup_shop_backend.repository.ProductSpecifications.*;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final CategoryService categoryService;


    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository,
                          ProductImageRepository productImageRepository,
                          ProductAttributeRepository productAttributeRepository,
                          CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productImageRepository = productImageRepository;
        this.productAttributeRepository = productAttributeRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(ProductFilter filter, Pageable pageable) {
        // в каталоге только товары в продаже; остальные условия добавляем, если фильтр задан
        List<Specification<Product>> conditions = new ArrayList<>();
        conditions.add(isActive());

        if (filter.category() != null && !filter.category().isBlank()) {
            conditions.add(inCategories(categoryService.findSelfAndDescendantIds(filter.category())));
        }
        if (filter.brand() != null && !filter.brand().isEmpty()) {
            conditions.add(hasBrands(filter.brand()));
        }
        if (filter.minPrice() != null) {
            conditions.add(priceFrom(filter.minPrice()));
        }
        if (filter.maxPrice() != null) {
            conditions.add(priceTo(filter.maxPrice()));
        }
        if (Boolean.TRUE.equals(filter.inStock())) {
            conditions.add(inStock());
        }
        if (Boolean.TRUE.equals(filter.onSale())) {
            conditions.add(onSale());
        }
        if (filter.boardType() != null && !filter.boardType().isBlank()) {
            conditions.add(hasBoardType(Product.BoardType.valueOf(filter.boardType())));
        }
        if (filter.q() != null && !filter.q().isBlank()) {
            conditions.add(matchesText(filter.q().trim()));
        }
        if (filter.id() != null && !filter.id().isEmpty()) {
            conditions.add(hasIds(filter.id()));
        }

        return productRepository.findAll(Specification.allOf(conditions), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse findByID(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден: " + id));
        return toResponse(product);
   }

    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Product not found: " + slug));
        return toResponse(product);
    }

   @Transactional
   public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        // slug задаётся один раз: при переименовании старые ссылки на товар должны работать
        product.setSlug(SlugUtil.uniqueSlug(request.name(), "product", productRepository::existsBySlug));
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCompareAtPrice(request.compareAtPrice());
        product.setQuantity(request.quantity());

        if(request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена: " + request.categoryId()));
            product.setCategory(category);
        }

        if(request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new NotFoundException("Бренд не найден: " + request.brandId()));
            product.setBrand(brand);
        }

        if(request.boardType() != null ) {
            product.setBoardType(Product.BoardType.valueOf(request.boardType()));
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);

   }

   @Transactional
   public ProductResponse update(Long id, ProductRequest request ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCompareAtPrice(request.compareAtPrice());
        product.setQuantity(request.quantity());

        if(request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена: " + request.categoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        if (request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new NotFoundException("Бренд не найден: " + request.brandId()));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        // null при обновлении — осознанное «не доска», а не «не трогать»
        if (request.boardType() != null) {
            product.setBoardType(Product.BoardType.valueOf(request.boardType()));
        } else {
            product.setBoardType(null);
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);
   }

   @Transactional
   public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден: " + id));
        product.setStatus("DISCONTINUED");
        productRepository.save(product);
   }

    private ProductResponse toResponse(Product p) {
        List<ProductImageResponse> images = productImageRepository
                .findByProductIdOrderBySortOrderAsc(p.getId())
                .stream()
                .map(img -> new ProductImageResponse(
                        img.getId(),
                        img.getImageUrl(),
                        img.isPrimary(),
                        img.getSortOrder()))
                .toList();

        List<ProductAttributeResponse> attributes = productAttributeRepository
                .findByProductId(p.getId())
                .stream()
                .map(attr -> new ProductAttributeResponse(
                        attr.getAttributeName(),
                        attr.getAttributeValue()))
                .toList();

        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getDescription(),
                p.getPrice(),
                p.getCompareAtPrice(),
                p.getQuantity(),
                p.getBrand() != null ? p.getBrand().getName() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getBoardType() != null ? p.getBoardType().name() : null,
                p.getStatus(),
                images,
                attributes
        );
    }

    //admin
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllForAdmin(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

}
