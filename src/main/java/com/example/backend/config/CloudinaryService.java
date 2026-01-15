package com.example.backend.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings({ "null", "rawtypes" })
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, String> uploadImage(
            MultipartFile file,
            String folder) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không hợp lệ");
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"));

        Map<String, String> result = new HashMap<>();
        result.put("url", uploadResult.get("secure_url").toString());
        result.put("publicId", uploadResult.get("public_id").toString());

        return result;
    }

    public void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank())
            return;
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
