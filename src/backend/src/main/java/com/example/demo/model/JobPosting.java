package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "job_postings")
public class JobPosting {

    // Values of the status column. Job seekers can only apply while a posting is open.
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer id;

    @Column(name = "employer_id", nullable = false)
    private Integer employerId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String salary;

    @Column(name = "required_skills", columnDefinition = "TEXT", nullable = false)
    private String requiredSkills;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "application_deadline", nullable = false)
    private LocalDate applicationDeadline;

    private String status;

    private String location;

    // Job search filters (V10). Null on postings created before they existed.
    private String industry;

    @Column(name = "job_type")
    private String jobType;

    // Read-only link so job search can show and search the company name
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", insertable = false, updatable = false)
    private EmployerUser employer;

    protected JobPosting() {}

    public JobPosting(Integer employerId, String title, String department, String salary,
                      String requiredSkills, String description, LocalDate applicationDeadline,
                      String location, String industry, String jobType) {
        this.employerId = employerId;
        this.title = title;
        this.department = department;
        this.salary = salary;
        this.requiredSkills = requiredSkills;
        this.description = description;
        this.applicationDeadline = applicationDeadline;
        this.location = location;
        this.industry = industry;
        this.jobType = jobType;
        this.status = OPEN;
    }

    public Integer getId() { return id; }
    public Integer getEmployerId() { return employerId; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public String getSalary() { return salary; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getDescription() { return description; }
    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public String getLocation() { return location; }
    public String getIndustry() { return industry; }
    public String getJobType() { return jobType; }

    // Only loaded when the query fetches the employer (see JobPostingRepository)
    public String getCompanyName() { return employer == null ? null : employer.getCompanyName(); }

    // Rows created before the status column existed count as open
    public String getStatus() { return status == null ? OPEN : status; }

    public void setStatus(String status) { this.status = status; }

    // Open and the deadline hasn't passed (applying on the deadline day is allowed)
    public boolean isAcceptingApplications(LocalDate today) {
        return OPEN.equals(getStatus()) && applicationDeadline != null && !applicationDeadline.isBefore(today);
    }
}
