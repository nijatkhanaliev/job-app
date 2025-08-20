package com.job.dao.repository;

import com.job.dao.entity.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {

    List<CV> findByUserId(Long userId);

}

