package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.BrandRequest;
import com.supshop.sup_shop_backend.dto.BrandResponse;
import com.supshop.sup_shop_backend.model.Brand;
import com.supshop.sup_shop_backend.repository.BrandRepository;
import org.apache.coyote.Response;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandService {
    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional
    public List<BrandResponse> getAll() {
        return brandRepository.findAll(Sort.by("name")).stream()
                .map(this::toResponse)
                .toList();
    }

    public BrandResponse create(BrandRequest request) {
        String name = request.name().trim();

        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Бренд уже существует: " + name);
        }

        Brand brand = new Brand();
        brand.setName(name);
        brand.setDescription(request.description());

        return toResponse(brandRepository.save(brand));

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

