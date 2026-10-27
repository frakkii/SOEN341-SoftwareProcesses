
package com.example.demo.repository;

import com.example.demo.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Integer> {

    @Override
    @EntityGraph(attributePaths = "employer")
    java.util.Optional<JobPosting> findById(Integer postId);

    @EntityGraph(attributePaths = "employer")
    @Query("""
            SELECT j FROM JobPosting j
            WHERE j.status = true
              AND j.deadline >= :today
              AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.employer.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:industry IS NULL OR LOWER(j.industry) LIKE LOWER(CONCAT('%', :industry, '%')))
              AND (:jobType IS NULL OR LOWER(j.jobType) LIKE LOWER(CONCAT('%', :jobType, '%')))
            ORDER BY j.postedDate DESC
            """)
    List<JobPosting> findActivePostings(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("industry") String industry,
            @Param("jobType") String jobType,
            @Param("today") LocalDate today
    );
}
