package com.job.model.dto.response;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class ExtractedData {
    private List<String> skills;
    private List<Experience> experience;
}

