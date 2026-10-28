package com.example.demo.repository;

import com.example.demo.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByPersonIdAndPostId(Integer personId, Integer postId);

    // A job seeker's applications, most recent first
    List<Application> findByPersonIdOrderByAppliedAtDesc(Integer personId);

    // Only finds the application if it belongs to this job seeker
    Optional<Application> findByIdAndPersonId(Long id, Integer personId);

    // job_postings is managed by the job posting feature, so applications only
    // read it directly to check the posting before applying
    @Query(value = "SELECT COUNT(*) FROM job_postings WHERE post_id = :postId", nativeQuery = true)
    long countPostings(@Param("postId") Integer postId);

    // Open (status = true) and the deadline hasn't passed
    @Query(value = "SELECT COUNT(*) FROM job_postings WHERE post_id = :postId AND status = TRUE "
            + "AND deadline >= CURRENT_DATE", nativeQuery = true)
    long countOpenPostings(@Param("postId") Integer postId);
   @Query(value = """
            SELECT COUNT(*)
            FROM application a
            JOIN job_postings jp ON jp.post_id = a.post_id
            WHERE a.id = :applicationId
              AND jp.employer_id = :employerId
            """, nativeQuery = true)
    long countApplicationOwnedByEmployer(
            @Param("applicationId") Long applicationId,
            @Param("employerId") Integer employerId);
}
