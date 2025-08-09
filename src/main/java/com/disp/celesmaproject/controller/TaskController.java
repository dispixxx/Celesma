package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.service.CommentService;
import com.disp.celesmaproject.service.CustomUserDetailsService;
import com.disp.celesmaproject.service.ProjectService;
import com.disp.celesmaproject.service.TaskService;
import com.disp.celesmaproject.util.AuthenticationFacade;
import com.disp.celesmaproject.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    @ModelAttribute("user")
    public User addUserToModel() {
        String username = authenticationFacade.getAuthenticatedUsername();
        return userDetailsService.getUserByUsername(username);
    }

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;


    @Autowired
    private AuthenticationFacade authenticationFacade;


    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private CommentService commentService;


    @GetMapping
    public String getProjectsTasks(@PathVariable Long projectId, Model model) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User currentUser = userDetailsService.getUserByUsername(username);

        List<Task> projectTasks = taskService.getTasksByProjectId(projectId);
        Project project = projectService.getProjectById(projectId);
        List<Task> createdTasksInProject =taskService.getTasksByCreatorIdAndProject(currentUser.getId(), projectId); // Задачи, где пользователь создатель
        List<Task> assignedTasksInProject =taskService.getTasksByAssigneeIdAndProjectId(currentUser.getId(), projectId); // Задачи, где пользователь исполнитель

        List<User> projectUsers = project.getMembers().stream()
                .map(ProjectMember::getUser)
                .toList();

        boolean isMember = projectUsers.contains(currentUser);

        model.addAttribute("createdTasks", createdTasksInProject);
        model.addAttribute("assignedTasks", assignedTasksInProject);
        model.addAttribute("allTasks", projectTasks);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("projectId", project.getId());
        model.addAttribute("project", project);
        model.addAttribute("isMember", isMember);

        return "project_tasks";
    }

    @GetMapping("/{id}")
    public String getTaskDetails(@PathVariable Long id, @PathVariable Long projectId,  @RequestParam(defaultValue = "desc") String sortOrder, Model model) {
        Task task = taskService.getTaskById(id);
        List<Comment> comments = commentService.getCommentsByTaskId(id);
        String username = authenticationFacade.getAuthenticatedUsername();
        User currentUser = userDetailsService.getUserByUsername(username);
        Project project = projectService.getProjectById(projectId);
        List<TaskHistory> history= taskService.getTaskHistory(id, sortOrder);
        List<User> projectUsers = project.getMembers().stream()
                .map(ProjectMember::getUser)
                .toList();
        boolean isMember = projectUsers.contains(currentUser);
        boolean isAdminOrModerator = false;
        if (isMember) {
            ProjectRole role = project.getMemberRole(currentUser);
            isAdminOrModerator = role == ProjectRole.ADMIN || role == ProjectRole.MODERATOR;
        }
        User creator = task.getCreator();
        User assignee = task.getAssignee();
        boolean isCreatorOrAssignee = currentUser.equals(creator) || currentUser.equals(assignee);

        if (!isAdminOrModerator) {
            return "redirect:/projects/" + projectId; // Если нет, перенаправляем на страницу проекта
        }

        model.addAttribute("task", task);
        model.addAttribute("taskStatus",TaskStatus.values());
        model.addAttribute("projectId",projectId);
        model.addAttribute("isAdminOrModerator", isAdminOrModerator);
        model.addAttribute("isCreatorOrAssignee", isCreatorOrAssignee);
        model.addAttribute("comments", comments);
        model.addAttribute("taskHistory", history);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("user", currentUser);
        model.addAttribute("project", project);
        model.addAttribute("isMember", isMember);
        return "task";
    }

    @PostMapping("/{id}/status")
    public String changeTaskStatus(@PathVariable Long id, @RequestParam TaskStatus status, @PathVariable Long projectId ) {
        taskService.changeTaskStatus(id, status);
        return "redirect:/projects/" + projectId + "/tasks/" + id;
    }


    @GetMapping("/create")
    public String showCreateTaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        Project project = projectService.getProjectById(projectId);
        String username = authenticationFacade.getAuthenticatedUsername();
        User currentUser = userDetailsService.getUserByUsername(username);
        List<ProjectMember>  projectMembers = projectService.getSortedProjectMembers(project.getId());
        List<User> projectUsers = projectService.getConvertedProjectMembersToUsers(projectMembers);
        boolean isMember = projectService.isMember(projectUsers, currentUser);
//        User currentUser = userRepository.findByUsername(username).isPresent() ? userRepository.findByUsername(username).get() : null;
        model.addAttribute("members", project.getMembers());
        model.addAttribute("projectName", project.getName());
        model.addAttribute("creatorId", Objects.requireNonNull(currentUser).getId());
        model.addAttribute("project", project);
        model.addAttribute("isMember", isMember);
        return "task_create";
    }

    @PostMapping("/create")
    public String createTask(@PathVariable Long projectId,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam(required = false) Long assigneeId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             @RequestParam TaskPriority priority,
                             @AuthenticationPrincipal Object principal) { // Изменяем тип на Object

        String username;
        User creator;

        if (principal instanceof UserDetails) {
            // Обычная аутентификация
            username = ((UserDetails) principal).getUsername();
            creator = userDetailsService.getUserByUsername(username);
        } else if (principal instanceof OAuth2User) {
            // OAuth2 аутентификация (Google)
            String email = ((OAuth2User) principal).getAttribute("email");
            username = email.split("@")[0];
            creator = userDetailsService.getUserByEmail(email);
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }
        if (creator == null) {
            throw new UsernameNotFoundException(username);
        }

        Project project = projectService.getProjectById(projectId);
        taskService.createTask(title, description, project, creator, assigneeId, endDate, TaskPriority.valueOf(priority.name()));
        return "redirect:/projects/" + projectId;
    }


}