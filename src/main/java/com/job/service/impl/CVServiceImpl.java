package com.job.service.impl;

import com.job.dao.entity.CV;
import com.job.dao.entity.User;
import com.job.dao.repository.CVRepository;
import com.job.dao.repository.UserRepository;
import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CVServiceImpl {

    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final ImageKit imageKit;

    public CV uploadCV(MultipartFile file, String email) throws Exception {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        String base64File = Base64.getEncoder().encodeToString(file.getBytes());

        FileCreateRequest request = new FileCreateRequest(base64File, file.getOriginalFilename());

        Result result = imageKit.upload(request);

        CV cv = CV.builder()
                .url(result.getUrl())
                .user(user)
                .status(1)
                .build();

        return cvRepository.save(cv);
    }
}
