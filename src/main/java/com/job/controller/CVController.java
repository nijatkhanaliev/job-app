package com.job.controller;

import com.job.dao.entity.CV;
import com.job.service.impl.CVServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/cvs")
@RequiredArgsConstructor
public class CVController {

    private final CVServiceImpl cvService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    @Operation(summary = "Upload CV")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).body("Unauthorized");
            }
            String email = authentication.getName();
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            CV savedCV = cvService.uploadCV(file, email);
            return ResponseEntity.ok(savedCV);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload CV: " + e.getMessage());
        }
    }
}
