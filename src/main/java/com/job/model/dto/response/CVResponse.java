package com.job.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVResponse {
    private Long cvId;
    private Long userId;
    private String url;
    private int status; //1 active ,0 inactive
    private ExtractedData extractedData;
    private LocalDateTime uploadedAt;

}
