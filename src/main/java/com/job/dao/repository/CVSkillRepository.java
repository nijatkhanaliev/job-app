package com.job.dao.repository;

import com.job.dao.entity.CVSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVSkillRepository extends JpaRepository<CVSkill,Long> {

    List<CVSkill> findByCvId(Long cvId);

}
