package com.example.int221integratedkk1_backend.Filters;

import com.example.int221integratedkk1_backend.Entities.Account.Visibility;
import com.example.int221integratedkk1_backend.Entities.Taskboard.AccessRight;
import com.example.int221integratedkk1_backend.Entities.Taskboard.BoardEntity;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.BoardRepository;
import com.example.int221integratedkk1_backend.Services.Account.JwtTokenUtil;
import com.example.int221integratedkk1_backend.Services.Taskboard.CollabService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

//@Component
//@Slf4j
//public class VisibilityFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private BoardRepository boardRepository;
//
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
//
//    @Autowired
//    private CollabService collabService;
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String requestURI = request.getRequestURI();
//        String method = request.getMethod();
//
//        if (requestURI.matches("/v3/boards/([^/]+)(/.*)?")) {
//            String boardId = requestURI.split("/")[3];
//            Optional<BoardEntity> boardOptional = boardRepository.findById(boardId);
//
//            if (boardOptional.isPresent()) {
//                BoardEntity board = boardOptional.get();
//
//                if (board.getVisibility() == Visibility.PUBLIC && method.equals("GET")) {
//                    filterChain.doFilter(request, response);
//                    return;
//                }
//
//                String authorizationHeader = request.getHeader("Authorization");
//
//                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                    String jwtToken = authorizationHeader.substring(7);
//
//                    try {
//                        String userIdFromToken = jwtTokenUtil.getUserIdFromToken(jwtToken);
//
//
//                        if (board.getOwnerId().equals(userIdFromToken)) {
//                            filterChain.doFilter(request, response);
//                            return;
//                        }
//
//                        Optional<AccessRight> accessRightOpt = collabService.getAccessRight(boardId, userIdFromToken);
//                        if (accessRightOpt.isPresent()) {
//                            AccessRight accessRight = accessRightOpt.get();
//
//
//                            if (method.equals("DELETE") && requestURI.matches("/v3/boards/" + boardId + "/collabs/" + userIdFromToken)) {
//                                filterChain.doFilter(request, response);
//                                return;
//                            }
//
//                            if (accessRight == AccessRight.WRITE) {
//                                if (isBoardModificationRequest(requestURI, method)) {
//                                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions to modify board properties.");
//                                    return;
//                                }
//                                filterChain.doFilter(request, response);
//                                return;
//                            }
//
//                            if (accessRight == AccessRight.READ) {
//                                if (isWriteOperation(method)) {
//                                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions to modify this board.");
//                                    return;
//                                }
//
//                                filterChain.doFilter(request, response);
//                                return;
//                            }
//                        }
//
//                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this board.");
//                        return;
//
//                    } catch (ExpiredJwtException e) {
//                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired.");
//                        return;
//                    } catch (Exception e) {
//                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token.");
//                        return;
//                    }
//                } else {
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required.");
//                    return;
//                }
//            } else {
//                // Board not found
//                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Board not found.");
//                return;
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private boolean isWriteOperation(String method) {
//        return method.equals("POST") || method.equals("PUT") || method.equals("DELETE");
//    }
//
//    private boolean isBoardModificationRequest(String requestURI, String method) {
//        return method.equals("PATCH")  && requestURI.matches("/v3/boards/[^/]+/?$");
//    }
//}
@Component
@Slf4j
public class VisibilityFilter extends OncePerRequestFilter {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CollabService collabService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (requestURI.matches("/v3/boards/([^/]+)(/.*)?")) {
            String boardId = requestURI.split("/")[3];
            Optional<BoardEntity> boardOptional = boardRepository.findById(boardId);

            if (boardOptional.isPresent()) {
                BoardEntity board = boardOptional.get();

                // Allow unauthenticated access to public boards for GET requests
                if (board.getVisibility() == Visibility.PUBLIC && method.equals("GET")) {
                    filterChain.doFilter(request, response); // Allow public GET access
                    return;
                }

                // Private boards or non-GET methods require authentication
                String authorizationHeader = request.getHeader("Authorization");

                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    String jwtToken = authorizationHeader.substring(7);

                    try {
                        String userIdFromToken = jwtTokenUtil.getUserIdFromToken(jwtToken);

                        // Allow board owners full access
                        if (board.getOwnerId().equals(userIdFromToken)) {
                            filterChain.doFilter(request, response);
                            return;
                        }

                        // Check if user is a collaborator and get their access rights
                        Optional<AccessRight> accessRightOpt = collabService.getAccessRight(boardId, userIdFromToken);
                        if (accessRightOpt.isPresent()) {
                            AccessRight accessRight = accessRightOpt.get();

                            // Allow collaborators specific permissions based on their access rights
                            if (accessRight == AccessRight.WRITE) {
                                if (isBoardModificationRequest(requestURI, method)) {
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions to modify board properties.");
                                    return;
                                }
                                filterChain.doFilter(request, response);
                                return;
                            }

                            if (accessRight == AccessRight.READ) {
                                if (isWriteOperation(method)) {
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions to modify this board.");
                                    return;
                                }
                                filterChain.doFilter(request, response);
                                return;
                            }
                        }

                        // If the user is neither an owner nor a collaborator, block access
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this board.");
                        return;

                    } catch (ExpiredJwtException e) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired.");
                        return;
                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token.");
                        return;
                    }
                } else {
                    // If no token is provided, block access to private boards and non-GET requests on public boards
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required to access this board.");
                    return;
                }
            } else {
                // Board not found
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Board not found.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // Helper to check if the operation is a write operation
    private boolean isWriteOperation(String method) {
        return method.equals("POST") || method.equals("PUT") || method.equals("DELETE");
    }

    // Helper to identify if the request is for modifying board properties (like visibility or name)
    private boolean isBoardModificationRequest(String requestURI, String method) {
        return method.equals("PATCH") && requestURI.matches("/v3/boards/[^/]+/?$");
    }
}
