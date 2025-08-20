package com.job.service.impl;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class OpenAIServiceHelper {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIServiceHelper.class);

    private final OpenAiService openAiService;

    // Spring will inject the API key here
    public OpenAIServiceHelper(@Value("${openai.api-key}") String openAiApiKey) {
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            throw new IllegalArgumentException("OpenAI API key is not set!");
        }
        this.openAiService = new OpenAiService(openAiApiKey);
        logger.info("OpenAIService initialized successfully");
    }

    public String getParsedCV(String cvText) {
        String prompt = "Extract skills and work experience from this CV and return JSON like:\n" +
                "{ \"skills\": [\"Java\", \"Spring Boot\"], " +
                "\"experience\": [{\"company\": \"ABC Tech\", \"role\": \"Backend Developer\", \"years\": \"2022-2024\"}]} \n" +
                "CV Text:\n" + cvText;

        ChatMessage message = new ChatMessage("user", prompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(Collections.singletonList(message))
                .build();

        int retries = 3;
        while (retries > 0) {
            try {
                logger.info("Sending request to OpenAI. Remaining retries: {}", retries);
                ChatCompletionResult result = openAiService.createChatCompletion(request);

                if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                    String output = result.getChoices().get(0).getMessage().getContent();
                    logger.info("Received response from OpenAI. Length: {}", output.length());
                    return output;
                } else {
                    logger.error("No output returned from OpenAI");
                    throw new IllegalStateException("No output returned from GPT");
                }

            } catch (retrofit2.HttpException e) {
                if (e.code() == 429) {
                    retries--;
                    logger.warn("Rate limit exceeded (429). Waiting 2 seconds before retry...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new RuntimeException("OpenAI API error: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse CV with OpenAI: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("Failed after 3 retries due to rate limits (429).");
    }

}