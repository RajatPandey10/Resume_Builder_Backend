package in.RajatPandey.resumebuilderapi.service;

import in.RajatPandey.resumebuilderapi.Documents.Resume;
import in.RajatPandey.resumebuilderapi.dto.AuthResponse;
import in.RajatPandey.resumebuilderapi.dto.CreateResumeRequest;
import in.RajatPandey.resumebuilderapi.repository.ResumeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AuthService authService;

    public Resume createResume(CreateResumeRequest request, Object principalObject) {

       Resume newResume = new Resume();
       AuthResponse response = authService.getProfile(principalObject);
        log.info("Creating new resume for userId: {}", response.getId());
       newResume.setUserId(response.getId());
       newResume.setTitle(request.getTitle());

//       Setting default resume data
        log.debug("Setting default fields for resume with title: {}", request.getTitle());
        setDefaultResumeData(newResume);

        log.info("Resume created successfully with id: {}", newResume.getId());
        return resumeRepository.save(newResume);

    }

    private void setDefaultResumeData(Resume newResume) {

        newResume.setProfileInfo(new Resume.ProfileInfo());
        newResume.setContactInfo(new Resume.ContactInfo());
        newResume.setWorkExperience(new ArrayList<>());
        newResume.setEducation(new ArrayList<>());
        newResume.setSkill(new ArrayList<>());
        newResume.setProject(new ArrayList<>());
        newResume.setCertification(new ArrayList<>());
        newResume.setLanguages(new ArrayList<>());
        newResume.setInterests(new ArrayList<>());

    }


    public List<Resume> getUserResumes(Object principal) {
        AuthResponse response = authService.getProfile(principal);
        log.info("Fetching resumes for userId: {}", response.getId());

        List<Resume> resumes = resumeRepository.findByUserIdOrderByUpdatedAtDesc(response.getId());

        log.debug("Found {} resumes for userId {}", resumes.size(), response.getId());
        return resumes;
    }

    public Resume getResumeById(String resumeId, Object principal) {
        AuthResponse response = authService.getProfile(principal);
        log.info("Fetching resume by id: {} for userId: {}", resumeId, response.getId());
        Resume existingResume = resumeRepository
                .findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> {
                    log.error("Resume not found with id: {} for userId: {}", resumeId, response.getId());
                    return new RuntimeException("Resume not found");
                });

        log.debug("Resume found: {}", existingResume.getId());
        return existingResume;

    }

    public Resume updataResume(String resumeId, Resume updatedData, Object principal) {
        AuthResponse response = authService.getProfile(principal);
        log.info("Updating resume id: {} for userId: {}", resumeId, response.getId());

        Resume existingResume = resumeRepository
                .findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> {
                    log.error("Cannot update. Resume not found: {} for userId: {}", resumeId, response.getId());
                    return new RuntimeException("Resume not found");
                });

        log.debug("Applying update fields to resume id: {}", resumeId);
        existingResume.setTitle(updatedData.getTitle());
        existingResume.setThumbnailLink(updatedData.getThumbnailLink());
        existingResume.setTemplate(updatedData.getTemplate());
        existingResume.setProfileInfo(updatedData.getProfileInfo());
        existingResume.setContactInfo(updatedData.getContactInfo());
        existingResume.setWorkExperience(updatedData.getWorkExperience());
        existingResume.setEducation(updatedData.getEducation());
        existingResume.setSkill(updatedData.getSkill());
        existingResume.setProject(updatedData.getProject());
        existingResume.setCertification(updatedData.getCertification());
        existingResume.setLanguages(updatedData.getLanguages());
        existingResume.setInterests(updatedData.getInterests());

        Resume updatedResume = resumeRepository.save(existingResume);

        log.info("Resume updated successfully: {}", resumeId);
        return updatedResume;
    }

    public void deleteResume(String resumeId, Object principal) {

        AuthResponse response = authService.getProfile(principal);
        log.info("Deleting resume id: {} for userId: {}", resumeId, response.getId());

        Resume existingResume = resumeRepository
                .findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> {
                    log.error("Cannot delete. Resume not found: {} for userId: {}", resumeId, response.getId());
                    return new RuntimeException("Resume not found");
                });

        resumeRepository.delete(existingResume);
        log.info("Resume deleted successfully: {}", resumeId);
    }
}
