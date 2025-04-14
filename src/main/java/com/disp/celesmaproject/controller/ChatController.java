package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.model.*;
import com.disp.celesmaproject.util.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CustomUserDetailsService userDetailsService;


    @MessageMapping("/project-chat/{projectId}")
    @SendTo("/topic/project-chat/{projectId}")
    public ChatMessage handleChatMessage(
            @Payload ChatMessage message,
            @DestinationVariable Long projectId,
            Principal principal) throws AccessDeniedException {
        Project project = projectService.getProjectById(projectId);
        List<ProjectMember> members = projectService.getSortedProjectMembers(project.getId());
        List<User> projectUsers = projectService.getConvertedProjectMembersToUsers(members);
        String username = principal.getName();
        User currentUser = userDetailsService.getUserByUsername(username);
        boolean isMember = projectUsers.contains(currentUser);
        if (!isMember) {
            throw new AccessDeniedException("Not a project member");
        }

        // Добавляем информацию о времени
        message.setTimestamp(LocalDateTime.now());

        chatService.saveMessage(message);
        return message;
    }

    @GetMapping("/api/projects/{projectId}/chat-messages")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable Long projectId) throws AccessDeniedException {
        Project project = projectService.getProjectById(projectId);
        List<ProjectMember> members = projectService.getSortedProjectMembers(project.getId());
        List<User> projectUsers = projectService.getConvertedProjectMembersToUsers(members);
        boolean isMember = projectUsers.contains(userDetailsService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
        if (!isMember) {
            throw new AccessDeniedException("Not a project member");
        }
        return chatService.getMessagesByProject(projectId);
    }
}
