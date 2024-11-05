package com.example.int221integratedkk1_backend.Services.Taskboard;

import com.example.int221integratedkk1_backend.DTOS.CollabDTO;
import com.example.int221integratedkk1_backend.DTOS.CollabRequest;
import com.example.int221integratedkk1_backend.Entities.Taskboard.AccessRight;
import com.example.int221integratedkk1_backend.Entities.Taskboard.Collaborator;
import com.example.int221integratedkk1_backend.Entities.Taskboard.BoardEntity;
import com.example.int221integratedkk1_backend.Exception.CollaboratorAlreadyExistsException;
import com.example.int221integratedkk1_backend.Exception.ItemNotFoundException;
import com.example.int221integratedkk1_backend.Exception.UnauthorizedException;
import com.example.int221integratedkk1_backend.Repositories.Account.UserRepository;
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

//    public boolean isCollaborator(String boardId, String userId) {
//        return collabRepository.existsByBoardIdAndCollabsId(boardId, userId);
//    }


    public Optional<Collaborator> getCollaboratorByBoardIdAndCollabId(String boardId, String collabId) {
        return collabRepository.findByBoardIdAndCollabsId(boardId, collabId);
    }
    public Collaborator addCollaborator(String boardId, CollabRequest collabRequest)
            throws CollaboratorAlreadyExistsException, ItemNotFoundException {

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with ID: " + boardId));

        UsersEntity user = userService.findUserByEmail(collabRequest.getEmail());
        if (user == null) {
            throw new ItemNotFoundException("User not found with email: " + collabRequest.getEmail());
        }

        if (collabRepository.existsByBoardIdAndCollabsId(boardId, user.getOid())) {
            throw new CollaboratorAlreadyExistsException("Collaborator already exists for this board.");
        }


        if (board.getOwnerId().equals(user.getOid())) {
            throw new CollaboratorAlreadyExistsException("Board owner cannot be added as a collaborator.");
        }


        Collaborator collaborator = new Collaborator();
        collaborator.setBoardId(boardId);
        collaborator.setCollabsId(user.getOid());
        collaborator.setCollabsName(user.getName());
        collaborator.setCollabsEmail(user.getEmail());
        collaborator.setAccessLevel(collabRequest.getAccessRight());
        collaborator.setAddedOn(new Timestamp(System.currentTimeMillis()));

        collaborator.setOwnerId(board.getOwnerId());

        collabRepository.save(collaborator);

        return collaborator;
    }

    public List<BoardEntity> getBoardsWhereUserIsCollaborator(String userId) {
        List<Collaborator> collaborators = collabRepository.findByCollabsId(userId);
        List<String> boardIds = collaborators.stream().map(Collaborator::getBoardId).collect(Collectors.toList());
        return boardRepository.findAllById(boardIds);
    }

//    public boolean isCollaboratorWithAccess(String boardId, String userId, AccessRight requiredAccessRight) {
//        Optional<Collaborator> collaborator = collabRepository.findByBoardIdAndCollaboratorId(boardId, userId);
//
//        return collaborator.isPresent() && collaborator.get().getAccessLevel().equals(requiredAccessRight);
//    }
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

//    public ResponseEntity<?> removeCollaborator(String boardId, String collabOid, String userId) {
//        //logger.info("Attempting to remove collaborator: {}, by user: {} on board: {}", collabOid, userId, boardId);
//
//        // Retrieve the board
//        BoardEntity board = getBoardById(boardId);
//
//        // Find the collaborator to be removed
//        Collaborator collaborator = collabRepository.findByBoardIdAndCollabsId(boardId, collabOid)
//                .orElseThrow(() -> {
//                    //logger.warn("Collaborator with ID: {} not found on board: {}", collabOid, boardId);
//                    return new ItemNotFoundException("Collaborator not found on this board.");
//                });
//
//        // Check if the requester is the board owner
//        if (board.getOwnerId().equals(userId)) {
//            //logger.info("User {} is the owner of the board. Proceeding to remove collaborator.", userId);
//            // Allow the owner to remove any collaborator
//            collabRepository.delete(collaborator);
//            return ResponseEntity.ok("Collaborator removed successfully.");
//        }
//
//        // Check if the requester is a WRITE collaborator trying to remove another collaborator
//        Optional<AccessRight> requesterAccessRight = getAccessRight(boardId, userId);
//        if (requesterAccessRight.isPresent()) {
//            AccessRight accessRight = requesterAccessRight.get();
//            //logger.info("User {} has {} access on board {}", userId, accessRight, boardId);
//
//            if (accessRight == AccessRight.WRITE && !collabOid.equals(userId)) {
//                // WRITE collaborators cannot remove others, only themselves
//                //logger.warn("WRITE collaborator {} attempted to remove another collaborator {}. Returning 403 FORBIDDEN.", userId, collabOid);
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Insufficient permissions to remove this collaborator.");
//            }
//
//            if (accessRight == AccessRight.WRITE && collabOid.equals(userId)) {
//                // Allow WRITE collaborators to remove themselves (leave the board)
//                //logger.info("WRITE collaborator {} is removing themselves from the board {}", userId, boardId);
//                collabRepository.delete(collaborator);
//                return ResponseEntity.ok("You have left the board.");
//            }
//        } else {
//           // logger.warn("User {} is not a recognized collaborator on board {}", userId, boardId);
//            // Not a valid collaborator, return forbidden
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to remove this collaborator.");
//        }
//
//        // Default response if conditions above are not met
//        //logger.warn("Unexpected case for user {} attempting to remove collaborator {} on board {}. Returning 403 FORBIDDEN.", userId, collabOid, boardId);
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to remove this collaborator.");
//    }


    public ResponseEntity<?> removeCollaborator(String boardId, String collabId, String userId) {
        //logger.info("Attempting to remove collaborator: {}, by user: {} on board: {}", collabOid, userId, boardId);

        // Retrieve the board
        BoardEntity board = getBoardById(boardId);

        // Find the collaborator to be removed
        Collaborator collaborator = collabRepository.findByBoardIdAndCollabsId(boardId, collabId)
                .orElseThrow(() -> {
                    //logger.warn("Collaborator with ID: {} not found on board: {}", collabOid, boardId);
                    return new ItemNotFoundException("Collaborator not found on this board.");
                });


        // Check if the requester is the board owner
        if (board.getOwnerId().equals(userId)) {
            //logger.info("User {} is the owner of the board. Proceeding to remove collaborator.", userId);
            // Allow the owner to remove any collaborator
            collabRepository.delete(collaborator);
            return ResponseEntity.ok("Collaborator removed successfully.");
        }

        // Check if the requester is trying to remove themselves (leave the board)
        if (collabId.equals(userId)) {
            // Allow both READ and WRITE collaborators to remove themselves
            //logger.info("Collaborator {} is removing themselves from the board {}", userId, boardId);
            collabRepository.delete(collaborator);
            return ResponseEntity.ok("You have left the board.");
        }

        // If the user is not the owner and is not removing themselves, deny the action
        //logger.warn("User {} attempted to remove another collaborator {} without sufficient permissions. Returning 403 FORBIDDEN.", userId, collabOid);
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
