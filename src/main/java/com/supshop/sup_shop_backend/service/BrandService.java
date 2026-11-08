package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.BrandRequest;
import com.supshop.sup_shop_backend.dto.BrandResponse;
import com.supshop.sup_shop_backend.exception.ConflictException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.Brand;
import com.supshop.sup_shop_backend.repository.BrandRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandService {
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public BrandService(BrandRepository brandRepository, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAll() {
        return brandRepository.findAll(Sort.by("name")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        String name = request.name().trim();

        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("error.brand.exists", name);
        }

        Brand brand = new Brand();
        brand.setName(name);
        brand.setDescription(request.description());

        return toResponse(brandRepository.save(brand));
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.brand.notFound"));

        String name = request.name().trim();
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("error.brand.exists", name);
        }

        // save() не нужен: в транзакции Hibernate сам заметит изменения и сделает UPDATE
        brand.setName(name);
        brand.setDescription(request.description());
        return toResponse(brand);
    }

    @Transactional
    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new NotFoundException("error.brand.notFound");
        }
        // без этой проверки упадём на внешнем ключе products.brand_id и отдадим 500
        if (productRepository.existsByBrandId(id)) {
            throw new ConflictException("error.brand.hasProducts");
        }
        brandRepository.deleteById(id);
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getLogoUrl(),
                brand.getDescription()
        );
    }
}
