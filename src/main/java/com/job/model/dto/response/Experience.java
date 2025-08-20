package com.job.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class Experience {
    private String company;
    private String role;
    private String years;
}