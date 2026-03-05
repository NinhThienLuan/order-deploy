package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.dto.products.ProductDetailResponse;
import fsoft.franchise.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin product image management — requires FRANCHISE_ADMIN role (enforced via
 * SecurityConfig).
 *
 * GET /admin/v1/products/{productId}/images — list images
 * POST /admin/v1/products/{productId}/images — upload images (multipart)
 * PATCH /admin/v1/products/images/{imageId}/primary — set primary image
 * DELETE /admin/v1/products/images/{imageId} — delete single image
 * DELETE /admin/v1/products/{productId}/images — delete all images of a product
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin — Product Image", description = "Admin product image management API")
// TODO: re-enable before production — temporarily disabled for UI dev
// @PreAuthorize("hasRole('FRANCHISE_ADMIN')")
public class AdminProductImageController {

    private final ProductImageService productImageService;

    @GetMapping("/{productId}/images")
    @Operation(summary = "Get all images of a product")
    public ResponseEntity<ApiResponse<List<ProductDetailResponse.ImageInfo>>> getImages(
            HttpServletRequest request,
            @PathVariable UUID productId) {

        List<ProductDetailResponse.ImageInfo> result = productImageService.getImagesByProductId(productId);
        return ResponseEntity.ok(ApiResponse.<List<ProductDetailResponse.ImageInfo>>builder()
                .code(200)
                .message(CommonErrorCode.SUCCESS.getMessage())
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload images for a product (Cloudinary)")
    public ResponseEntity<ApiResponse<List<ProductDetailResponse.ImageInfo>>> uploadImages(
            HttpServletRequest request,
            @PathVariable UUID productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "true") boolean setPrimaryFirst) {

        List<ProductDetailResponse.ImageInfo> result = productImageService.uploadImages(productId, files,
                setPrimaryFirst);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<List<ProductDetailResponse.ImageInfo>>builder()
                        .code(201)
                        .message("Images uploaded successfully")
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @PatchMapping("/images/{imageId}/primary")
    @Operation(summary = "Set an image as the primary image for its product")
    public ResponseEntity<ApiResponse<ProductDetailResponse.ImageInfo>> setPrimary(
            HttpServletRequest request,
            @PathVariable UUID imageId) {

        ProductDetailResponse.ImageInfo result = productImageService.setPrimaryImage(imageId);
        return ResponseEntity.ok(ApiResponse.<ProductDetailResponse.ImageInfo>builder()
                .code(200)
                .message("Primary image updated")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Delete a single product image")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/images")
    @Operation(summary = "Delete all images of a product")
    public ResponseEntity<Void> deleteAllImages(@PathVariable UUID productId) {
        productImageService.deleteAllImagesByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}
