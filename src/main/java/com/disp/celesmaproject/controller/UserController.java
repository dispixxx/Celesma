package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.model.*;
import com.disp.celesmaproject.util.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

/*    //Свой профиль
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
    }*/

    @GetMapping("/user/profile/{username}")
    public String viewUserProfile(
            @PathVariable String username,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Получаем пользователя по username из URL
        User profileUser = userDetailsService.getUserByUsername(username);
        if (profileUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User '" + username + "' not found"
            );
        }

        // Текущий аутентифицированный пользователь
        User currentUser = userDetailsService.getUserByUsername(
                authenticationFacade.getAuthenticatedUsername()
        );

        // Проверка прав доступа (если нужно)
        boolean isOwner = currentUser != null && currentUser.getId().equals(profileUser.getId());
        model.addAttribute("isOwner", isOwner);

        // Заполнение модели
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("userFirstName", profileUser.getFirstName());
        model.addAttribute("userLastName", profileUser.getLastName());
        model.addAttribute("username", username);
        model.addAttribute("userEmail", profileUser.getEmail());

        // Статистика
        model.addAttribute("projectCount", projectService.getUserProjectCount(profileUser.getId()));
        model.addAttribute("taskCountAsAssignee", taskService.getTaskCountByAssignee(profileUser.getId()));
        model.addAttribute("taskCountAsCreator", taskService.getTaskCountByCreator(profileUser.getId()));
        model.addAttribute("completedTaskCount", taskService.getCompletedTaskCountByUser(profileUser.getId()));

        return "user_profile";
    }

    @PostMapping("/api/user/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileDTO userProfileDto) {
        userDetailsService.updateUserProfile(userProfileDto);
        return ResponseEntity.ok().build();
    }
}
