package com.example.demo.repository;

import com.example.demo.model.JobPosting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Integer> {
    List<JobPosting> findByEmployerId(Integer employerId);

    // Only finds the posting if this recruiter owns it
    Optional<JobPosting> findByIdAndEmployerId(Integer id, Integer employerId);

    // One posting with its employer, for the job detail page
    @EntityGraph(attributePaths = "employer")
    @Query("SELECT j FROM JobPosting j WHERE j.id = :id")
    Optional<JobPosting> findWithEmployerById(@Param("id") Integer id);

    // Job search: open postings whose deadline hasn't passed, newest first. Each filter is
    // optional (null) and matches part of the text, ignoring case. Postings without an
    // industry are matched on their department instead.
    @EntityGraph(attributePaths = "employer")
    @Query("""
            SELECT j FROM JobPosting j
            WHERE (j.status IS NULL OR j.status = 'Open')
              AND j.applicationDeadline >= :today
              AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.employer.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:industry IS NULL OR LOWER(COALESCE(j.industry, j.department)) LIKE LOWER(CONCAT('%', :industry, '%')))
              AND (:jobType IS NULL OR LOWER(j.jobType) LIKE LOWER(CONCAT('%', :jobType, '%')))
            ORDER BY j.id DESC
            """)
    List<JobPosting> findActivePostings(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("industry") String industry,
            @Param("jobType") String jobType,
            @Param("today") LocalDate today
    );
}
