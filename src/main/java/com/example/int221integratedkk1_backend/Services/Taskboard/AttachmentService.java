package com.example.int221integratedkk1_backend.Services.Taskboard;

import com.example.int221integratedkk1_backend.DTOS.AttachmentDTO;
import com.example.int221integratedkk1_backend.DTOS.AttachmentFileResponse;
import com.example.int221integratedkk1_backend.DTOS.AttachmentResponse;
import com.example.int221integratedkk1_backend.DTOS.AttachmentResponseDTO;
import com.example.int221integratedkk1_backend.Entities.Taskboard.AttachmentEntity;
import com.example.int221integratedkk1_backend.Entities.Taskboard.TaskEntity;
import com.example.int221integratedkk1_backend.Exception.ItemNotFoundException;
import com.example.int221integratedkk1_backend.Exception.ValidateInputException;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.AttachmentRepository;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttachmentService {
    @Autowired
    private TaskRepository repository;

    @Autowired
    private AttachmentRepository attachmentRepository;


    private static final int MAX_FILES = 10;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final String UPLOAD_DIR = new File("src/main/resources/attachments/").getAbsolutePath() + "/";


    public AttachmentFileResponse addAttachment(Integer taskId, MultipartFile[] files) throws IOException {
        AttachmentFileResponse attachmentFileResponse = new AttachmentFileResponse();
        HashMap<String, String> errors = new HashMap<>();
        Optional<TaskEntity> task = repository.findById(taskId);

        if (task.isEmpty()) {
            throw new ValidateInputException("Task with ID " + taskId + " does not exist.");
        }

        // Validate the total number of files being uploaded
        List<AttachmentEntity> existingAttachments = attachmentRepository.findByTaskId(taskId);
        if (files.length > MAX_FILES) {
            throw new ValidateInputException("Task cannot have more than " + MAX_FILES + " files. Current: "
                    + existingAttachments.size() + ", Trying to add: " + files.length);
        }

        // Directory for task-specific uploads
        File taskUploadDir = new File(UPLOAD_DIR + File.separator + taskId);
        if (!taskUploadDir.exists()) {
            taskUploadDir.mkdirs();
        }

        // List to store saved attachments and response data
        List<AttachmentResponseDTO> responseList = new ArrayList<>();

        for (MultipartFile file : files) {
            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                errors.put(file.getOriginalFilename(), "File exceeds the maximum size of " + MAX_FILE_SIZE / (1024 * 1024) + " MB.");
                continue;
            }

            // Check for duplicate filenames
            Optional<AttachmentEntity> duplicateFile = existingAttachments.stream()
                    .filter(attachment -> attachment.getFileName().equals(file.getOriginalFilename()))
                    .findFirst();
            if (duplicateFile.isPresent()) {
                errors.put(file.getOriginalFilename(), "File with the same name already exists.");
                continue;
            }

            try {
                // Save file to disk
                String filePath = taskUploadDir.getAbsolutePath() + File.separator + file.getOriginalFilename();
                file.transferTo(new File(filePath));

                // Save attachment details to the database
                AttachmentEntity attachment = new AttachmentEntity();
                attachment.setTask(task.get());
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileType(file.getContentType());
                attachment.setFilePath(filePath);
                attachment.setUploadedOn(OffsetDateTime.now());

                AttachmentEntity savedAttachment = attachmentRepository.save(attachment);

                // Add successful file details to the response list
                responseList.add(new AttachmentResponseDTO(savedAttachment.getFileName(), "Uploaded successfully"));

            } catch (IOException ex) {
                errors.put(file.getOriginalFilename(), "Failed to upload file due to server error.");
            }
        }

        // Add error details to the response list
        errors.forEach((fileName, errorMessage) ->
                responseList.add(new AttachmentResponseDTO(fileName, errorMessage))
        );

        attachmentFileResponse.setData(responseList);
        attachmentFileResponse.setStatus(200);
        attachmentFileResponse.setMessage("Save File Service");
        return attachmentFileResponse;
    }



    public AttachmentFileResponse deleteAttachment(Long attachmentId) {
        AttachmentEntity attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ValidateInputException("Attachment not found"));

        // ลบไฟล์จากระบบไฟล์
        File file = new File(attachment.getFilePath());
        if (file.exists() && !file.delete()) {
            throw new ValidateInputException("Failed to delete the file: " + file.getAbsolutePath());
        }

        File taskFolder = new File(UPLOAD_DIR + File.separator + attachment.getTask().getId());
        if (taskFolder.isDirectory() && taskFolder.list().length == 0) {
            taskFolder.delete(); // ลบโฟลเดอร์ถ้าว่าง
        }
        attachmentRepository.deleteById(attachmentId);

        // Return a success response
        AttachmentFileResponse response = new AttachmentFileResponse();
        response.setData(null); // No data for a delete operation
        response.setStatus(200); // HTTP OK status
        response.setMessage("Delete file success");

        return response;
    }


    public AttachmentResponse getAttachmentsByTaskId(int taskId) {
        List<AttachmentEntity> attachments = attachmentRepository.findByTaskId(taskId);

        List<AttachmentDTO> attachmentDtos = attachments.stream()
                .map(attachment -> {
                    //ถ้า deploy ต้องแก้
                    String fileUrl = "http://localhost:8080/api/attachment/" + taskId + "/" + attachment.getFileName();
                    return new AttachmentDTO(
                            attachment.getId(),
                            attachment.getFileName(),
                            attachment.getFileType(),
                            fileUrl,
                            attachment.getUploadedOn()
                    );
                })
                .collect(Collectors.toList());

        // สร้าง `AttachmentResponse` พร้อมข้อมูล
        int status = attachmentDtos.isEmpty() ? 204 : 200;
        String message = attachmentDtos.isEmpty()
                ? "No attachments found for task ID " + taskId
                : "Attachments retrieved successfully.";
        return new AttachmentResponse(attachmentDtos, status, message);
    }

}
