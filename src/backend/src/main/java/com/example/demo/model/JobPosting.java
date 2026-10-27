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

    protected JobPosting() {}

    public JobPosting(Integer employerId, String title, String department, String salary,
                      String requiredSkills, String description, LocalDate applicationDeadline,
                      String location) {
        this.employerId = employerId;
        this.title = title;
        this.department = department;
        this.salary = salary;
        this.requiredSkills = requiredSkills;
        this.description = description;
        this.applicationDeadline = applicationDeadline;
        this.location = location;
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

    // Rows created before the status column existed count as open
    public String getStatus() { return status == null ? OPEN : status; }

    public void setStatus(String status) { this.status = status; }
}
