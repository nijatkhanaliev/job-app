package com.job.dao.repository;

import com.job.dao.entity.OtpDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpDetailsRepository extends JpaRepository<OtpDetails, Long> {
    Optional<OtpDetails> findByEmail(String email);

    Optional<OtpDetails> findTopByEmailOrderByExpiryDateDesc(String email);

    void deleteAllByEmail(String email);
}
