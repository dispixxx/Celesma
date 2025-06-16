package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.model.*;
import com.disp.celesmaproject.service.ChatService;
import com.disp.celesmaproject.service.CustomUserDetailsService;
import com.disp.celesmaproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

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
    public ChatMessageDTO handleChatMessage(
            @Payload ChatMessageDTO messageDTO,
            @DestinationVariable Long projectId,
            Principal principal) throws AccessDeniedException {
        Project project = projectService.getProjectById(projectId);
        List<ProjectMember> members = projectService.getSortedProjectMembers(project.getId());
        List<User> projectUsers = projectService.getConvertedProjectMembersToUsers(members);
        String username = principal.getName();
        User currentUser = userDetailsService.getUserByUsername(username);

        if (!projectUsers.contains(currentUser)) {
            throw new AccessDeniedException("Not a project member");
        }

        // Create and save the message
        ChatMessage message = new ChatMessage();
        message.setProject(project);
        message.setSender(currentUser);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatService.saveMessage(message);

        // Convert to DTO for sending to clients
        return convertToDTO(savedMessage);
    }

    @GetMapping("/api/projects/{projectId}/chat-messages")
    @ResponseBody
    public List<ChatMessageDTO> getChatHistory(@PathVariable Long projectId) throws AccessDeniedException {
        Project project = projectService.getProjectById(projectId);
        List<ProjectMember> members = projectService.getSortedProjectMembers(project.getId());
        List<User> projectUsers = projectService.getConvertedProjectMembersToUsers(members);
        boolean isMember = projectUsers.contains(
                userDetailsService.getUserByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName()
                )
        );

        if (!isMember) {
            throw new AccessDeniedException("Not a project member");
        }

        List<ChatMessage> messages = chatService.getMessagesByProject(projectId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setProjectId(message.getProject().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}
