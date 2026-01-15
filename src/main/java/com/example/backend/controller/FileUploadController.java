package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.config.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    // =========================
    // CATEGORY
    // =========================
    @PostMapping("/category")
    public ResponseEntity<?> uploadCategory(
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(
                    cloudinaryService.uploadImage(file, "categories"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Upload category thất bại");
        }
    }

    // =========================
    // PRODUCT
    // =========================
    @PostMapping("/product")
    public ResponseEntity<?> uploadProduct(
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(
                    cloudinaryService.uploadImage(file, "products"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Upload product thất bại");
        }
    }

    // =========================
    // BRAND
    // =========================
    @PostMapping("/brand")
    public ResponseEntity<?> uploadBrand(
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(
                    cloudinaryService.uploadImage(file, "brands"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Upload brand thất bại");
        }
    }

    // =========================
    // USER AVATAR
    // =========================
    @PostMapping("/user")
    public ResponseEntity<?> uploadUser(
            @RequestParam("file") MultipartFile file) {
        System.out.println(
                "Processing upload/user request for file: " + (file != null ? file.getOriginalFilename() : "null"));
        try {
            return ResponseEntity.ok(
                    cloudinaryService.uploadImage(file, "users/avatar"));
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Upload user thất bại: " + e.getMessage());
        }
    }

    // =========================
    // POST
    // =========================
    @PostMapping("/post")
    public ResponseEntity<?> uploadPost(
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(
                    cloudinaryService.uploadImage(file, "posts"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Upload post thất bại");
        }
    }
}
