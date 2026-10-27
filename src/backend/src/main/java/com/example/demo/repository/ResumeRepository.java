package com.example.demo.repository;

import com.example.demo.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // The resume's details without the file itself, so showing them doesn't load the whole file
    interface ResumeInfo {
        String getFileName();

        Integer getFileSize();

        Instant getUploadedAt();
    }

    // The job seeker's current resume (the newest, in case an older row was left current)
    Optional<ResumeInfo> findFirstInfoByPersonIdAndCurrentTrueOrderByUploadedAtDesc(Integer personId);

    Optional<Resume> findFirstByPersonIdAndCurrentTrueOrderByUploadedAtDesc(Integer personId);

    boolean existsByPersonIdAndCurrentTrue(Integer personId);

    // Keeps the job seeker's earlier resumes but stops them being current, before a new upload
    @Modifying
    @Query("UPDATE Resume r SET r.current = false WHERE r.personId = :personId AND r.current = true")
    int retireCurrent(@Param("personId") Integer personId);
}
