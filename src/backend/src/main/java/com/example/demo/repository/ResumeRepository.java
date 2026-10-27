package com.example.demo.repository;

import com.example.demo.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // The resume's details without the file itself, so showing them doesn't load the whole file
    interface ResumeInfo {
        Long getId();

        String getFileName();

        Integer getFileSize();

        Instant getUploadedAt();

        Boolean getCurrent();
    }

    Optional<ResumeInfo> findInfoByPersonIdAndCurrentTrue(Integer personId);

    Optional<Resume> findByPersonIdAndCurrentTrue(Integer personId);

    Optional<Resume> findByIdAndPersonId(Long id, Integer personId);

    Optional<Resume> findFirstByPersonIdOrderByUploadedAtDesc(Integer personId);

    List<ResumeInfo> findAllByPersonIdOrderByUploadedAtDesc(Integer personId);

    boolean existsByPersonIdAndCurrentTrue(Integer personId);
}
