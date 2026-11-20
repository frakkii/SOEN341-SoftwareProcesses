package com.example.demo.repository;

import com.example.demo.model.PersonUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PersonUserRepository extends JpaRepository<PersonUser, Integer> {

    // Ignore case so rows added outside the app (e.g. in the Supabase dashboard) still match
    Optional<PersonUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select person.id from PersonUser person")
    List<Integer> findAllIds();
}
