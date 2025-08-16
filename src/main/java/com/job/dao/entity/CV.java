package com.job.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import com.job.common.BaseEntity;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cv")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CV extends BaseEntity {

    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int status; //1 active
}
