package in.RajatPandey.resumebuilderapi.controller;

import in.RajatPandey.resumebuilderapi.Documents.Resume;
import in.RajatPandey.resumebuilderapi.dto.CreateResumeRequest;
import in.RajatPandey.resumebuilderapi.service.FileUploadService;
import in.RajatPandey.resumebuilderapi.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static in.RajatPandey.resumebuilderapi.utils.AppConstants.*;
@RestController
@RequestMapping(RESUME)
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<?> createResume(@Valid @RequestBody CreateResumeRequest request,
                                          Authentication authentication){

        log.info("Create resume request received for user: {}", authentication.getName());
        Resume newResume = resumeService.createResume(request,authentication.getPrincipal());

        log.info("Resume created successfully with id: {}", newResume.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }

    @GetMapping
    public ResponseEntity<?> getUserResume(Authentication authentication){

        log.info("fetching all resume for user: {}",authentication.getName());
        List<Resume> resumes = resumeService.getUserResumes(authentication.getPrincipal());

        log.info("found {} for user: {}",resumes.size(),authentication.getName());
        return ResponseEntity.ok(resumes);
    }

    @GetMapping(ID)
    public ResponseEntity<?> getResumeById(@PathVariable String id,
                                           Authentication authentication){
        log.info("fetching resume with id: {} for user: {}",id,authentication.getName());
        Resume existingResume = resumeService.getResumeById(id,authentication.getPrincipal());
        log.info("Resume fetched successfully for id: {}", id);
        return ResponseEntity.ok(existingResume);

    }

    @PutMapping(ID)
    public ResponseEntity<?> updateResume(@PathVariable String id,
                                          @RequestBody Resume updatedData,
                                          Authentication authentication){

        log.info("Update request received for resume id: {} by user: {}", id, authentication.getName());
        Resume updatedResume = resumeService.updataResume(id,updatedData,authentication.getPrincipal());
        log.info("Resume updated successfully for id: {}", id);
        return  ResponseEntity.ok(updatedResume);
    }

    @PutMapping(UPLOAD_IMAGES)
    public ResponseEntity<?> uploadResumeImages(@PathVariable String id,
                                                @RequestPart(value = "thumbnail", required = false)MultipartFile thumbnail,
                                                @RequestPart(value = "profileImage", required = false)MultipartFile profileImage,
                                                Authentication authentication) throws IOException {

        log.info("Uploading images for resume id: {} by user: {}", id, authentication.getName());

        if (thumbnail != null) {
            log.debug("Thumbnail file received: {}", thumbnail.getOriginalFilename());
        }
        if (profileImage != null) {
            log.debug("Profile image received: {}", profileImage.getOriginalFilename());
        }
        Map<String,String> response = fileUploadService.uploadResumeImages(id,authentication.getPrincipal(),thumbnail,profileImage);
        log.info("Images uploaded successfully for resume id: {}", id);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping(ID)
    public ResponseEntity<?> deleteResume(@PathVariable String id,
                                          Authentication authentication){

        log.info("Delete request received for resume id: {} by user: {}", id, authentication.getName());

        resumeService.deleteResume(id, authentication.getPrincipal());

        log.info("Resume deleted successfully for id: {}", id);

        return ResponseEntity.ok(Map.of("message","Resume deleted successfully"));
    }
}
