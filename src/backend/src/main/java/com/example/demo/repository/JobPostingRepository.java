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
    // optional ('' means no filter) and matches part of the text, ignoring case. Postings
    // without an industry are matched on their department instead. Filters are never null:
    // Postgres can't tell the type of a null parameter and fails with "lower(bytea)".
    @EntityGraph(attributePaths = "employer")
    @Query("""
            SELECT j FROM JobPosting j
            WHERE (j.status IS NULL OR j.status = 'Open')
              AND j.applicationDeadline >= :today
              AND (:keyword = '' OR LOWER(j.title) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(j.description) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(j.requiredSkills) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(j.employer.companyName) LIKE CONCAT('%', LOWER(:keyword), '%'))
              AND (:location = '' OR LOWER(j.location) LIKE CONCAT('%', LOWER(:location), '%'))
              AND (:industry = '' OR LOWER(COALESCE(j.industry, j.department)) LIKE CONCAT('%', LOWER(:industry), '%'))
              AND (:jobType = '' OR LOWER(j.jobType) LIKE CONCAT('%', LOWER(:jobType), '%'))
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
