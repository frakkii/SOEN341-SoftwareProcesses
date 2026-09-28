package com.example.demo.repository;

import com.example.demo.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Integer> {

    // The resume's details without the file itself, so showing them doesn't load the whole file
    interface ResumeInfo {
        String getFileName();

        Integer getFileSize();

        Instant getUploadedAt();
    }

    Optional<ResumeInfo> findInfoByPersonId(Integer personId);
}
