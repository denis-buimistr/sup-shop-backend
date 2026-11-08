package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.ProductImageResponse;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.ProductImage;
import com.supshop.sup_shop_backend.repository.ProductImageRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import com.supshop.sup_shop_backend.exception.ConflictException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final S3Service s3Service;

    public ProductImageService(ProductImageRepository productImageRepository,
                               ProductRepository productRepository,
                               S3Service s3Service) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.s3Service = s3Service;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findByProduct(Long productId) {
        ensureProductExists(productId);

        return productImageRepository.findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductImageResponse upload(Long productId,
                                       MultipartFile file,
                                       boolean primary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Товар не найден: " + productId));

        String folder = "products/" + productId;
        String url = s3Service.uploadFile(file, folder);

        if (primary) {
            demoteExistingPrimary(productId);
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(url);
        image.setPrimary(primary);
        image.setSortOrder(nextSortOrder(productId));

        return toResponse(productImageRepository.save(image));
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException(
                        "Изображение не найдено: " + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    "Изображение не принадлежит товару " + productId);
        }

        s3Service.deleteFile(extractKey(image.getImageUrl()));
        productImageRepository.delete(image);

        // если удалили главное, главным становится следующее, и номера идут без дырок
        applyOrder(productImageRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .filter(img -> !img.getId().equals(imageId))
                .toList());
    }

    // порядок приходит из админки после перетаскивания, первое фото становится главным
    @Transactional
    public List<ProductImageResponse> reorder(Long productId, List<Long> imageIds) {
        ensureProductExists(productId);
        Map<Long, ProductImage> byId = productImageRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .collect(Collectors.toMap(ProductImage::getId, Function.identity()));

        // список должен совпадать с фото в БД один в один, иначе админка показывает старое состояние
        if (imageIds.size() != byId.size() || !byId.keySet().equals(new HashSet<>(imageIds))) {
            throw new ConflictException("Photos of this product have changed, reload the page");
        }

        List<ProductImage> ordered = imageIds.stream().map(byId::get).toList();
        applyOrder(ordered);
        return ordered.stream().map(this::toResponse).toList();
    }

    // сущности внутри транзакции, поэтому save не нужен, Hibernate сам запишет изменения при коммите
    private void applyOrder(List<ProductImage> images) {
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setSortOrder(i);
            images.get(i).setPrimary(i == 0);
        }
    }

    private void demoteExistingPrimary(Long productId) {
        productImageRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .filter(ProductImage::isPrimary)
                .forEach(img -> {
                    img.setPrimary(false);
                    productImageRepository.save(img);
                });
    }

    private int nextSortOrder(Long productId) {
        return productImageRepository.findByProductIdOrderBySortOrderAsc(productId).size();
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Товар не найден: " + productId);
        }
    }

    private String extractKey(String url) {
        int index = url.indexOf(".amazonaws.com/");
        if (index < 0) {
            throw new IllegalStateException("Некорректный URL изображения: " + url);
        }
        return url.substring(index + ".amazonaws.com/".length());
    }

    private ProductImageResponse toResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.isPrimary(),
                image.getSortOrder()
        );
    }
}