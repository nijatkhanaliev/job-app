package com.job.dao.entity;

import com.job.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cv_experience")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CVExperience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id",nullable = false)
    private CV cv;

    @Column(nullable = false)
    private String company;
    @Column(nullable = false)
    private String role;
    @Column(nullable = true)
    private String years; // example: "2022-2024"

}
