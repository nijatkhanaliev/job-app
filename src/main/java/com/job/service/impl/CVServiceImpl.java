package com.job.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.job.dao.entity.CV;
import com.job.dao.entity.CVExperience;
import com.job.dao.entity.CVSkill;
import com.job.dao.entity.User;
import com.job.dao.repository.CVExperienceRepository;
import com.job.dao.repository.CVRepository;
import com.job.dao.repository.CVSkillRepository;
import com.job.dao.repository.UserRepository;
import com.job.model.dto.response.CVResponse;
import com.job.model.dto.response.Experience;
import com.job.model.dto.response.ExtractedData;
import com.job.util.CVParser;
import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CVServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(CVServiceImpl.class);
    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final ImageKit imageKit;
    private final CVSkillRepository cvSkillRepository;
    private final CVExperienceRepository cvExperienceRepository;
    private final OpenAIServiceHelper openAIServiceHelper;


    @Transactional
    public CVResponse uploadCV(MultipartFile file, String email) throws Exception {
        logger.info("Starting CV upload for user: {}", email);

        User user = getUserByEmail(email);
        checkFileNotEmpty(file);

        CV savedCV = saveFileToImageKit(file, user);
        String cvText = extractCVText(file);

        String jsonResponse = openAIServiceHelper.getParsedCV(cvText);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> parsedData = mapper.readValue(jsonResponse, Map.class);

        saveSkillsAndExperience(savedCV, parsedData);

        ExtractedData extractedData = buildExtractedData(parsedData);
        CVResponse response = buildCVResponse(savedCV, user, extractedData);

        logger.info("CV upload completed successfully for user: {}", email);
        return response;
    }


    private User getUserByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", email);
                    return new IllegalArgumentException("User not found: " + email);
                });
    }

    private void checkFileNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            logger.error("Uploaded file is empty");
            throw new IllegalArgumentException("File is empty");
        }
        logger.debug("File check passed: {}", file.getOriginalFilename());
    }

    private CV saveFileToImageKit(MultipartFile file, User user) throws Exception {
        logger.info("Uploading file to ImageKit: {}", file.getOriginalFilename());
        String base64File = Base64.getEncoder().encodeToString(file.getBytes());
        var request = new FileCreateRequest(base64File, file.getOriginalFilename());
        var result = imageKit.upload(request);

        CV savedCV = CV.builder()
                .url(result.getUrl())
                .user(user)
                .status(1)
                .build();
        cvRepository.save(savedCV);
        logger.info("File uploaded and CV saved. URL: {}", result.getUrl());
        return savedCV;
    }

    private String extractCVText(MultipartFile file) throws Exception {
        logger.info("Extracting text from CV: {}", file.getOriginalFilename());
        String text = CVParser.extractText(file.getInputStream());
        logger.debug("Extracted text length: {}", text.length());
        return text;
    }

    private void saveSkillsAndExperience(CV savedCV, Map<String, Object> parsedData) {
        logger.info("Saving skills and experience to database for CV ID: {}", savedCV.getId());

        List<String> skills = (List<String>) parsedData.get("skills");
        List<Map<String, String>> experienceList = (List<Map<String, String>>) parsedData.get("experience");

        if (skills != null) {
            skills.forEach(skill -> {
                cvSkillRepository.save(CVSkill.builder().cv(savedCV).skillName(skill).build());
                logger.debug("Saved skill: {}", skill);
            });
        }

        if (experienceList != null) {
            experienceList.forEach(exp -> {
                cvExperienceRepository.save(CVExperience.builder()
                        .cv(savedCV)
                        .company(exp.get("company"))
                        .role(exp.get("role"))
                        .years(exp.get("years"))
                        .build());
                logger.debug("Saved experience: {} at {}", exp.get("role"), exp.get("company"));
            });
        }

        logger.info("Skills and experience saved successfully");
    }

    private ExtractedData buildExtractedData(Map<String, Object> parsedData) {
        logger.info("Building ExtractedData object");
        List<String> skills = (List<String>) parsedData.get("skills");
        List<Map<String, String>> experienceList = (List<Map<String, String>>) parsedData.get("experience");

        return ExtractedData.builder()
                .skills(skills)
                .experience(experienceList == null ? List.of() : experienceList.stream()
                        .map(exp -> Experience.builder()
                                .company(exp.get("company"))
                                .role(exp.get("role"))
                                .years(exp.get("years"))
                                .build())
                        .toList())
                .build();
    }

    private CVResponse buildCVResponse(CV savedCV, User user, ExtractedData extractedData) {
        logger.info("Building CVResponse object for CV ID: {}", savedCV.getId());
        return CVResponse.builder()
                .cvId(savedCV.getId())
                .userId(user.getId())
                .url(savedCV.getUrl())
                .status(1)
                .uploadedAt(savedCV.getCreatedAt())
                .extractedData(extractedData)
                .build();
    }
}
