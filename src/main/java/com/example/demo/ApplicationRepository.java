package com.example.demo;
//import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

//@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
}
