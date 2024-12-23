package com.example.int221integratedkk1_backend.Services.Taskboard;

import com.example.int221integratedkk1_backend.DTOS.CollabDTO;
import com.example.int221integratedkk1_backend.DTOS.CollabRequest;
import com.example.int221integratedkk1_backend.Entities.Taskboard.AccessRight;
import com.example.int221integratedkk1_backend.Entities.Taskboard.Collaborator;
import com.example.int221integratedkk1_backend.Entities.Taskboard.BoardEntity;
import com.example.int221integratedkk1_backend.Exception.CollaboratorAlreadyExistsException;
import com.example.int221integratedkk1_backend.Exception.ItemNotFoundException;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.CollabRepository;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.BoardRepository;
import com.example.int221integratedkk1_backend.Entities.Account.UsersEntity;
import com.example.int221integratedkk1_backend.Services.Account.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollabService {

    @Autowired
    private CollabRepository collabRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardRepository boardRepository;

    public List<CollabDTO> getCollaborators(String boardId) {
        List<Collaborator> collaborators = collabRepository.findByBoardId(boardId);
        return collaborators.stream().map(collab -> {
            CollabDTO collaboratorDTO = new CollabDTO();
            collaboratorDTO.setOid(collab.getCollabsId());
            collaboratorDTO.setName(collab.getCollabsName());
            collaboratorDTO.setEmail(collab.getCollabsEmail());
            collaboratorDTO.setAccessRight(collab.getAccessLevel());
            collaboratorDTO.setAddedOn(collab.getAddedOn());
            return collaboratorDTO;
        }).collect(Collectors.toList());
    }

    public Optional<Collaborator> getCollaboratorByBoardIdAndCollabId(String boardId, String collabId) {
        return collabRepository.findByBoardIdAndCollabsId(boardId, collabId);
    }

    public Collaborator addCollaborator(String boardId, CollabRequest collabRequest)
            throws CollaboratorAlreadyExistsException, ItemNotFoundException {

        // Get the BoardEntity by ID
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with ID: " + boardId));

        // Find the user based on email
        UsersEntity user = userService.findUserByEmail(collabRequest.getEmail());
        if (user == null) {
            throw new ItemNotFoundException("User not found with email: " + collabRequest.getEmail());
        }

        // Check if the user is already a collaborator for the board
        if (collabRepository.existsByBoardIdAndCollabsId(boardId, user.getOid())) {
            throw new CollaboratorAlreadyExistsException("Collaborator already exists for this board.");
        }

        // Check if the user is the board owner
        if (board.getOwnerId().equals(user.getOid())) {
            throw new CollaboratorAlreadyExistsException("Board owner cannot be added as a collaborator.");
        }

        // Create new Collaborator and set attributes
        Collaborator collaborator = new Collaborator();
        collaborator.setBoard(board); // Set BoardEntity instead of boardId
        collaborator.setCollabsId(user.getOid());
        collaborator.setCollabsName(user.getName());
        collaborator.setCollabsEmail(user.getEmail());
        collaborator.setAccessLevel(collabRequest.getAccessRight());
        collaborator.setAddedOn(new Timestamp(System.currentTimeMillis()));

        collaborator.setOwnerId(board.getOwnerId());

        // Save the collaborator
        collabRepository.save(collaborator);

        return collaborator;
    }


    public List<BoardEntity> getBoardsWhereUserIsCollaborator(String userId) {
        List<Collaborator> collaborators = collabRepository.findByCollabsId(userId);
        List<String> boardIds = collaborators.stream().map(c -> c.getBoard().getId()).collect(Collectors.toList()); // Changed to getBoard().getId()
        return boardRepository.findAllById(boardIds);
    }


    public Optional<Collaborator> getCollaboratorByBoardIdAndCollaboratorId(String boardId, String userId) {
        return collabRepository.findByBoardIdAndCollabsId(boardId, userId);
    }

    public boolean isCollaborator(String boardId, String userId) {
        return collabRepository.existsByBoardIdAndCollabsId(boardId, userId);
    }

    public Collaborator updateCollaboratorAccess(String boardId, String collabId, String accessRight) {

        Optional<Collaborator> collaboratorOpt = getCollaboratorByBoardIdAndCollaboratorId(boardId, collabId);
        Collaborator collaborator = collaboratorOpt.orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));


        collaborator.setAccessLevel(AccessRight.valueOf(accessRight.toUpperCase()));
        return collabRepository.save(collaborator);
    }

    public ResponseEntity<?> removeCollaborator(String boardId, String collabId, String userId) {

        Optional<BoardEntity> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Board not found.");
        }
        BoardEntity board = boardOpt.get();

        Optional<Collaborator> collaboratorOpt = collabRepository.findByBoardIdAndCollabsId(boardId, collabId);
        if (collaboratorOpt.isEmpty()) {
            if (board.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Collaborator not found.");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to remove this collaborator.");
        }

        Collaborator collaborator = collaboratorOpt.get();

        if (!board.getOwnerId().equals(userId) && !collabId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to remove this collaborator.");
        }

        if (!board.getOwnerId().equals(userId) && !collabId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to remove this collaborator.");
        }

        if (board.getOwnerId().equals(userId)) {
            collabRepository.delete(collaborator);
            return ResponseEntity.ok("Collaborator removed successfully.");
        }

        if (collabId.equals(userId)) {
            collabRepository.delete(collaborator);
            return ResponseEntity.ok("You have left the board.");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to remove this collaborator.");
    }



    private BoardEntity getBoardById(String boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found with ID: " + boardId));
    }

    public Optional<AccessRight> getAccessRight(String boardId, String userId) {
        Optional<Collaborator> collaboratorOpt = collabRepository.findByBoardIdAndCollabsId(boardId, userId);
        return collaboratorOpt.map(Collaborator::getAccessLevel);
    }
}
