package fsoft.franchise.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String uploadImage(MultipartFile file, String folder);

    void deleteImage(String publicId);

    String extractPublicId(String imageUrl);
}
