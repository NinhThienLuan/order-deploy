package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.ProductErrorCode;
import fsoft.franchise.dto.products.ProductDetailResponse;
import fsoft.franchise.entity.ProductEntity;
import fsoft.franchise.entity.ProductImageEntity;
import fsoft.franchise.repository.ProductImageRepository;
import fsoft.franchise.service.CloudinaryService;
import fsoft.franchise.service.ProductImageService;
import fsoft.franchise.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private static final String CLOUDINARY_FOLDER = "products";

    private final ProductImageRepository productImageRepository;
    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailResponse.ImageInfo> getImagesByProductId(UUID productId) {
        productService.getActiveProductOrThrow(productId);
        return productImageRepository.findAllByProductId(productId).stream()
                .map(this::toImageInfo)
                .toList();
    }

    @Override
    @Transactional
    public List<ProductDetailResponse.ImageInfo> uploadImages(UUID productId, List<MultipartFile> files,
            boolean setPrimaryFirst) {
        ProductEntity product = productService.getActiveProductOrThrow(productId);

        if (files == null || files.isEmpty()) {
            throw new ApiException(ProductErrorCode.IMAGE_NOT_FOUND, "No files provided");
        }

        boolean hasExistingPrimary = productImageRepository
                .findByProductIdAndIsPrimaryTrue(productId)
                .isPresent();

        // Upload all to Cloudinary first, collect URLs
        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    throw new ApiException(ProductErrorCode.IMAGE_NOT_FOUND, "File cannot be empty");
                }
                String url = cloudinaryService.uploadImage(file, CLOUDINARY_FOLDER);
                uploadedUrls.add(url);
                log.info("Uploaded image to Cloudinary: {}", url);
            }
        } catch (ApiException e) {
            rollbackCloudinaryUploads(uploadedUrls);
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload image to Cloudinary: {}", e.getMessage(), e);
            rollbackCloudinaryUploads(uploadedUrls);
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }

        // Persist to DB
        List<ProductImageEntity> savedImages = new ArrayList<>();
        for (int i = 0; i < uploadedUrls.size(); i++) {
            boolean isPrimary = setPrimaryFirst && i == 0 && !hasExistingPrimary;
            ProductImageEntity image = ProductImageEntity.builder()
                    .product(product)
                    .imageUrl(uploadedUrls.get(i))
                    .isPrimary(isPrimary)
                    .build();
            savedImages.add(productImageRepository.save(image));
        }

        return savedImages.stream().map(this::toImageInfo).toList();
    }


    @Override
    @Transactional
    public ProductDetailResponse.ImageInfo setPrimaryImage(UUID imageId, UUID productId) {
        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ApiException(ProductErrorCode.IMAGE_NOT_FOUND));

        productId = image.getProduct().getId();

        productImageRepository.findByProductIdAndIsPrimaryTrue(productId)
                .ifPresent(oldPrimary -> {
                    if (!oldPrimary.getId().equals(imageId)) {
                        oldPrimary.setIsPrimary(false);
                        productImageRepository.save(oldPrimary);
                    }
                });

        image.setIsPrimary(true);
        return toImageInfo(productImageRepository.save(image));
    }

    @Override
    @Transactional
    public void deleteImage(UUID imageId) {
        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ApiException(ProductErrorCode.IMAGE_NOT_FOUND));

        String publicId = cloudinaryService.extractPublicId(image.getImageUrl());
        productImageRepository.delete(image);
        try {
            cloudinaryService.deleteImage(publicId);
        } catch (Exception e) {
            log.warn("Image deleted from DB but failed to delete from Cloudinary (publicId={}): {}", publicId,
                    e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteAllImagesByProductId(UUID productId) {
        productService.getActiveProductOrThrow(productId);
        List<ProductImageEntity> images = productImageRepository.findAllByProductId(productId);
        productImageRepository.deleteAll(images);
        for (ProductImageEntity image : images) {
            try {
                cloudinaryService.deleteImage(cloudinaryService.extractPublicId(image.getImageUrl()));
            } catch (Exception e) {
                log.warn("Failed to delete Cloudinary image: {}", e.getMessage());
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void rollbackCloudinaryUploads(List<String> uploadedUrls) {
        for (String url : uploadedUrls) {
            try {
                cloudinaryService.deleteImage(cloudinaryService.extractPublicId(url));
                log.info("Rolled back Cloudinary image: {}", url);
            } catch (Exception ex) {
                log.warn("Failed to rollback Cloudinary image {}: {}", url, ex.getMessage());
            }
        }
    }

    private ProductDetailResponse.ImageInfo toImageInfo(ProductImageEntity img) {
        return ProductDetailResponse.ImageInfo.builder()
                .id(img.getId())
                .imageUrl(img.getImageUrl())
                .isPrimary(img.getIsPrimary())
                .build();
    }
}
