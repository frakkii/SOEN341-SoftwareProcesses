package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    protected JobPosting() {}

    public JobPosting(Integer employerId, String title, String department, String salary,
                      String requiredSkills, String description, LocalDate applicationDeadline) {
        this.employerId = employerId;
        this.title = title;
        this.department = department;
        this.salary = salary;
        this.requiredSkills = requiredSkills;
        this.description = description;
        this.applicationDeadline = applicationDeadline;
    }

    public Integer getId() { return id; }
    public Integer getEmployerId() { return employerId; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public String getSalary() { return salary; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getDescription() { return description; }
    public LocalDate getApplicationDeadline() { return applicationDeadline; }
}
