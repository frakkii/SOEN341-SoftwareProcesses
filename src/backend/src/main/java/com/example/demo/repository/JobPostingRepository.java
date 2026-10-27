package com.example.demo.repository;

import com.example.demo.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Integer> {
    List<JobPosting> findByEmployerId(Integer employerId);

    // Only finds the posting if this recruiter owns it
    Optional<JobPosting> findByIdAndEmployerId(Integer id, Integer employerId);
}
