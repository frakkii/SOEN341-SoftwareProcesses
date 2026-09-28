package com.example.demo.service;

import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Application;
import com.example.demo.repository.ApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
public class ApplicationService{
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void uploadResume(Long id, MultipartFile file) {
        Application app = findApplication(id);
        if (app.getisSubmitted()) {
            throw new ApplicationException("Cannot upload document for a submitted application.");
        }
        if (file.isEmpty() || file == null) {
            throw new ApplicationException("Uploaded file is empty.");
        }
        try {
            app.uploadDocument(file.getBytes());
        } catch (IOException e) {
            throw new ApplicationException("Error reading the uploaded file.");
        }
        applicationRepository.save(app);

    }
    public void submitApplication(Long id) {
        Application app = findApplication(id);
        if (app.getName() == null || app.getName().isEmpty()) {
            throw new ApplicationException("Application name is required.");
        }
        if (app.getDocument() == null) {
            throw new ApplicationException("Application resume is required.");
        }
        if (app.getisSubmitted()) {
            throw new ApplicationException("Application has already been submitted.");
        }
        app.submit();
        applicationRepository.save(app);
    }
    public void approveApplication(Long id) {
        Application app = findApplication(id);
        if (!app.getisSubmitted()) {
            throw new ApplicationException("Cannot approve an application that has not been submitted.");
        }
        if ("Approved".equals(app.getStatus())) {
            throw new ApplicationException("Application has already been approved.");
        }
        if ("Rejected".equals(app.getStatus())) {
            throw new ApplicationException("Cannot approve an application that has been rejected.");
        }
        app.approve();
        applicationRepository.save(app);
    }
    public void rejectApplication(Long id) {
        Application app = findApplication(id);
        if (!app.getisSubmitted()) {
            throw new ApplicationException("Cannot reject an application that has not been submitted.");
        }
        if ("Approved".equals(app.getStatus())) {
            throw new ApplicationException("Cannot reject an application that has been approved.");
        }
        if ("Rejected".equals(app.getStatus())) {
            throw new ApplicationException("Application has already been rejected.");
        }
        app.reject();
        applicationRepository.save(app);
    }
    private Application findApplication(Long id) {
        Optional<Application> applicationOptional = applicationRepository.findById(id);
        if (!applicationOptional.isPresent()) {
            throw new ApplicationException("Application with ID " + id + " not found.");
        }
        return applicationOptional.get();
    }
    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestParam("name") String name){
        Application application = new Application(name);
        Application saved = applicationRepository.save(application);
        return ResponseEntity.ok(saved);
    }

}


