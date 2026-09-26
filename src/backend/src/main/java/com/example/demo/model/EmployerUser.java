package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// A recruiter. The application and posting counters in employer_user are left unmapped for now.
@Entity
@Table(name = "employer_user")
public class EmployerUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employer_id")
    private Integer id;

    private String email;

    @Column(name = "recruiter_name")
    private String recruiterName;

    @Column(name = "recruiter_surname")
    private String recruiterSurname;

    @Column(name = "company_name")
    private String companyName;

    private String country;

    @Column(name = "province_state")
    private String provinceState;

    private String city;
    private String address;

    @Column(name = "postal_code_zip")
    private String postalCodeZip;

    private String phone;
    private String password;

    protected EmployerUser() {
    }

    public EmployerUser(String email, String recruiterName, String recruiterSurname, String companyName,
                        String country, String provinceState, String city, String address,
                        String postalCodeZip, String phone, String password) {
        this.email = email;
        this.recruiterName = recruiterName;
        this.recruiterSurname = recruiterSurname;
        this.companyName = companyName;
        this.country = country;
        this.provinceState = provinceState;
        this.city = city;
        this.address = address;
        this.postalCodeZip = postalCodeZip;
        this.phone = phone;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public String getRecruiterSurname() {
        return recruiterSurname;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCountry() {
        return country;
    }

    public String getProvinceState() {
        return provinceState;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCodeZip() {
        return postalCodeZip;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }
}
