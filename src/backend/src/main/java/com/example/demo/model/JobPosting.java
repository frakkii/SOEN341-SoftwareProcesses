
package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "employer_id")
    private Integer employerId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "location")
    private String location;

    @Column(name = "industry")
    private String industry;

    @Column(name = "job_type")
    private String jobType;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", insertable = false, updatable = false)
    private EmployerUser employer;

    protected JobPosting() {
    }

    public Integer getPostId() {
        return postId;
    }

    public Integer getEmployerId() {
        return employerId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getPostedDate() {
        return postedDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getIndustry() {
        return industry;
    }

    public String getJobType() {
        return jobType;
    }

    public String getCompanyName() {
        return employer == null ? null : employer.getCompanyName();
    }
}
