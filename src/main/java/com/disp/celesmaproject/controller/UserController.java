package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.model.*;
import com.disp.celesmaproject.util.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Controller
public class UserController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @GetMapping("/user/profile")
    public String viewUserProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User currentUser = userDetailsService.getUserByUsername(username);
        String userEmail = currentUser.getEmail();
        String userFirstName = currentUser.getFirstName();
        String userLastName = currentUser.getLastName();

        // Получаем данные о проектах и задачах
        int projectCount = projectService.getUserProjectCount(currentUser.getId());
        int taskCountAsAssignee = taskService.getTaskCountByAssignee(currentUser.getId());
        int taskCountAsCreator = taskService.getTaskCountByCreator(currentUser.getId());
        int completedTaskCount = taskService.getCompletedTaskCountByUser(currentUser.getId());

        model.addAttribute("userFirstName", Objects.requireNonNullElse(userFirstName, "[FIRSTNAME]"));
        model.addAttribute("userLastName", Objects.requireNonNullElse(userLastName, "[LASTNAME]"));
        model.addAttribute("username", username);
        model.addAttribute("userEmail", userEmail);

        // Добавляем данные для статистики
        model.addAttribute("projectCount", projectCount);
        model.addAttribute("taskCountAsAssignee", taskCountAsAssignee);
        model.addAttribute("taskCountAsCreator", taskCountAsCreator);
        model.addAttribute("completedTaskCount", completedTaskCount);

        return "user_profile";
    }

    @PostMapping("/api/user/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileDto userProfileDto) {
        userDetailsService.updateUserProfile(userProfileDto);
        return ResponseEntity.ok().build();
    }
}
