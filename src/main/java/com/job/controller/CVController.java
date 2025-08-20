package com.job.controller;

import com.job.model.dto.response.CVResponse;
import com.job.service.impl.CVServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/cvs")
@RequiredArgsConstructor
public class CVController {

    private final CVServiceImpl cvService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            CVResponse response = cvService.uploadCV(file, email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user")
    public ResponseEntity<List<CVResponse>> getUserCVs() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<CVResponse> userCVs = cvService.getUserCVs(email);
            return ResponseEntity.ok(userCVs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
