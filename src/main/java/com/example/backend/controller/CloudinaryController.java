package com.example.backend.controller;

import com.example.backend.config.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        try {
            Map<String, String> result = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/image/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable String publicId) {
        try {
            cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
