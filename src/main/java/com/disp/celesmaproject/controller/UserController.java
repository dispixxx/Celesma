package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.model.*;
import com.disp.celesmaproject.util.AuthenticationFacade;
import com.disp.celesmaproject.util.FileStorageService;
import com.disp.celesmaproject.util.YandexDiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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

    @Autowired
    private YandexDiskService yandexDiskService;

    @Autowired
    private FileStorageService fileStorageService;

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

        String highQualityProfileUserPictureUrl = profileUser.getAvatarUrl() != null ? profileUser.getAvatarUrl().replace("s96-c", "s512-c"): null;

        // Заполнение модели
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("profileUserBigAvatarUrl",highQualityProfileUserPictureUrl);
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

    @PostMapping(value = "/api/user/updateProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDTO> updateProfile(
            @RequestParam String username,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestPart(required = false) MultipartFile avatarFile) {

        try {

//            String localFileUrl = fileStorageService.storeFile(avatarFile); //Сохранение файла на локальной папке

            UserProfileDTO profileDto = new UserProfileDTO();
            profileDto.setUsername(username);
            profileDto.setFirstName(firstName);
            profileDto.setLastName(lastName);

            if (avatarFile != null && !avatarFile.isEmpty()) {
                validateAvatarFile(avatarFile);
                String avatarUrl = yandexDiskService.uploadFileToYandexDisk(avatarFile,username).getYandexDiskUrl();
                if (avatarUrl != null || !Objects.equals(avatarUrl, "")) {
                    profileDto.setAvatarUrl(avatarUrl);
                }
                else profileDto.setAvatarUrl(null);
            }

            User updatedUser = userDetailsService.updateUserProfile(profileDto);
            return ResponseEntity.ok(convertToDto(updatedUser));

        } catch (IOException e) {
//            logger.error("Error updating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UserProfileDTO());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new UserProfileDTO());
        }
    }

    private UserProfileDTO convertToDto(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    private void validateAvatarFile(MultipartFile file) {
        // Максимальный размер файла - 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
        }

        // Проверяем, что файл является изображением
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }
    }
}
