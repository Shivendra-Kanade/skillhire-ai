package com.skillhire.repository;

import com.skillhire.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);

    Page<Job> findByRecruiterId(Long recruiterId, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)")
    Page<Job> searchJobs(@Param("location") String location,
                          @Param("title") String title,
                          @Param("experienceLevel") Job.ExperienceLevel experienceLevel,
                          Pageable pageable);

    @Query("SELECT j FROM Job j JOIN j.skills s WHERE s.name IN :skills AND j.status = 'ACTIVE'")
    List<Job> findBySkillsIn(@Param("skills") List<String> skills);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND j.createdAt >= :since ORDER BY j.createdAt DESC")
    List<Job> findRecentActiveJobs(@Param("since") LocalDateTime since);

    long countByRecruiterId(Long recruiterId);

    long countByStatus(Job.JobStatus status);
}
