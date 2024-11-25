package com.example.int221integratedkk1_backend.Controllers.Taskboard;

import com.example.int221integratedkk1_backend.DTOS.AttachmentFileResponse;
import com.example.int221integratedkk1_backend.DTOS.AttachmentResponse;
import com.example.int221integratedkk1_backend.DTOS.AttachmentResponseDTO;
import com.example.int221integratedkk1_backend.Entities.Taskboard.AttachmentEntity;
import com.example.int221integratedkk1_backend.Services.Taskboard.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/attachment")
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23kk1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th:8080", "http://ip23kk1.sit.kmutt.ac.th:8080"})

public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @PostMapping("/{taskId}/attachments")
    public ResponseEntity<?> addAttachment(@PathVariable int taskId, @RequestParam("files") MultipartFile[] file) {
        try {
            AttachmentFileResponse attachment = attachmentService.addAttachment(taskId, file);
            return ResponseEntity.ok(attachment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            AttachmentFileResponse attachmentFileResponse = attachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.status(200).body(attachmentFileResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @GetMapping("/{taskId}/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable int taskId, @PathVariable String filename) throws IOException {
        // Define the directory for attachments
      
        // FE แก้เป็น filepath ตัวเอง
        final String UPLOAD_DIR = "/Users/HUAWEI/Documents/integrate2/Backend_Sprint4V3/src/main/resources/attachments";

        // Construct the full file path
        String filePath = UPLOAD_DIR + File.separator + taskId + File.separator + filename;
        File file = new File(filePath);

        // Check if the file exists
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filename);
        }

        // Load the file as a resource
        Resource resource = new UrlResource(file.toURI());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("Could not read file: " + filename);
        }

        // Determine file type dynamically
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Fallback to binary if type is unknown
        }

        // Return the file with appropriate headers
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<AttachmentResponse> getAttachmentsByTaskId(@PathVariable int taskId) {
        try {
            AttachmentResponse response = attachmentService.getAttachmentsByTaskId(taskId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AttachmentResponse(null, 500,"Internal Server Error"));
        }
    }

}
