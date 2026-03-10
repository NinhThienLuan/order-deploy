package fsoft.franchise.service;

import fsoft.franchise.dto.products.ProductDetailResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductImageService {

    List<ProductDetailResponse.ImageInfo> getImagesByProductId(UUID productId);

    List<ProductDetailResponse.ImageInfo> uploadImages(UUID productId, List<MultipartFile> files,
            boolean setPrimaryFirst);

    ProductDetailResponse.ImageInfo setPrimaryImage(UUID imageId, UUID productId);

    void deleteImage(UUID imageId);

    void deleteAllImagesByProductId(UUID productId);
}
