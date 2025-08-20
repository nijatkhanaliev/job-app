package com.job.dao.repository;

import com.job.dao.entity.CVExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVExperienceRepository extends JpaRepository<CVExperience, Long> {

    List<CVExperience> findByCvId(Long cvId);

}
