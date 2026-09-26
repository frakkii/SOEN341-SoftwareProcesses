package com.example.demo.repository;

import com.example.demo.model.EmployerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployerUserRepository extends JpaRepository<EmployerUser, Integer> {

    // Ignore case so rows added outside the app (e.g. in the Supabase dashboard) still match
    Optional<EmployerUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
