package fsoft.franchise.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import fsoft.franchise.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractPublicId(String imageUrl) {
        // URL format:
        // https://res.cloudinary.com/{cloud}/image/upload/v123456/{folder}/{filename}.ext
        // public_id = {folder}/{filename} (without extension)
        if (imageUrl == null || imageUrl.isBlank())
            return null;
        String[] parts = imageUrl.split("/upload/");
        if (parts.length < 2)
            return null;
        String afterUpload = parts[1];
        // Remove version prefix (v123456/)
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        // Remove extension
        int dotIndex = afterUpload.lastIndexOf(".");
        return dotIndex != -1 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }
}
