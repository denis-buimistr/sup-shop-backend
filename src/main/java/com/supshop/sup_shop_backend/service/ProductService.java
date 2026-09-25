package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.Brand;
import com.supshop.sup_shop_backend.model.Category;
import com.supshop.sup_shop_backend.dto.ProductRequest;
import com.supshop.sup_shop_backend.dto.ProductResponse;
import com.supshop.sup_shop_backend.repository.BrandRepository;
import com.supshop.sup_shop_backend.repository.CategoryRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    public Page<ProductResponse> findAll(Long categoryId, Long brandId, String boardType, Pageable pageable) {
        Page<Product> page;

        if (categoryId != null) {
            page = productRepository.findByCategoryId(categoryId, pageable);
        } else if (brandId != null) {
            page = productRepository.findByBrandId(brandId, pageable);
        } else if (boardType != null) {
            page = productRepository.findByBoardType(Product.BoardType.valueOf(boardType), pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return page.map(this::toResponse);
    }

   public ProductResponse findbyID(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден" + id));
        return toResponse(product);
   }

   public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());

        if(request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена: " + request.categoryId()));
            product.setCategory(category);
        }

        if(request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new RuntimeException("Бренд не найден: " + request.brandId()));
            product.setBrand(brand);
        }

        if(request.boardType() != null ) {
            product.setBoardType(Product.BoardType.valueOf(request.boardType()));
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);

   }

   public ProductResponse update(Long id, ProductRequest request ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());

        if(request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена: " + request.categoryId()));
        } else {
            product.setCategory(null);
        }

        if (request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new RuntimeException("Бренд не найден: " + request.brandId()));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        if (request.boardType() != null) {
            product.setBoardType(Product.BoardType.valueOf(request.boardType()));
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);
   }

   public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден: " + id));
        product.setStatus("DISCONTINUED");
        productRepository.save(product);
   }

   private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity(),
                p.getBrand() != null ? p.getBrand().getName() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getBoardType() != null ? p.getBoardType().name() : null,
                p.getStatus()
        );
   }

}
