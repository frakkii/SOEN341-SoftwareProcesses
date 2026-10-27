package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// A job seeker. The application counters in person_user are left unmapped for now.
@Entity
@Table(name = "person_user")
public class PersonUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Integer id;

    private String email;
    private String name;
    private String surname;
    private String country;

    @Column(name = "province_state")
    private String provinceState;

    private String city;
    private String address;

    @Column(name = "postal_code_zip")
    private String postalCodeZip;

    private String phone;
    private String password;

    // Written by the job seeker on their profile page; null until they fill them in
    private String headline;
    private String about;

    // Comma-separated, e.g. "Java,SQL,Git"
    private String skills;

    protected PersonUser() {
    }

    public PersonUser(String email, String name, String surname, String country, String provinceState,
                      String city, String address, String postalCodeZip, String phone, String password) {
        this.email = email;
        this.name = name;
        this.surname = surname;
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

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
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

    public String getHeadline() {
        return headline;
    }

    public String getAbout() {
        return about;
    }

    public String getSkills() {
        return skills;
    }

    public void updateProfile(String headline, String about, String skills,
                              String city, String provinceState, String country) {
        this.headline = headline;
        this.about = about;
        this.skills = skills;
        this.city = city;
        this.provinceState = provinceState;
        this.country = country;
    }
}
