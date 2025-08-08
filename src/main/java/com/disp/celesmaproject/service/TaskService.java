package com.disp.celesmaproject.service;


import com.disp.celesmaproject.model.*;
import com.disp.celesmaproject.repo.TaskHistoryRepository;
import com.disp.celesmaproject.repo.TaskRepository;
import com.disp.celesmaproject.repo.UserRepository;
import com.disp.celesmaproject.util.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    public void createTask(String title, String description, Project project, User creator, Long assigneeId, LocalDate endDate, TaskPriority priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCreatedAt(LocalDateTime.now());
        task.setProject(project);
        task.setCreator(creator);
        task.setEndDate(endDate);
        task.setPriority(priority);
        task.setStatus(TaskStatus.NEW);
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId).orElseThrow();
            task.setAssignee(assignee);
        }
        taskRepository.save(task);
    }

    public List<Task> getTasksByAssigneeIdAndProjectId(Long assigneeId, Long projectId) {
        return taskRepository.findByAssigneeIdAndProjectId(assigneeId, projectId);
    }

    public List<Task> getTasksByCreatorIdAndProject(Long creatorId, Long projectId) {
        return taskRepository.findByCreatorIdAndProjectId(creatorId, projectId);
    }

    public List<Task> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public List<Task> getTasksByCreatorId(Long creatorId) {
        return taskRepository.findByCreatorId(creatorId);
    }

    public List<Task> getTasksByAssigneeId(Long creatorId) {
        return taskRepository.findByAssigneeId(creatorId);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Задача не найдена"));
    }

    public int getTaskCountByAssignee(Long assigneeId) {
        return taskRepository.countByAssigneeId(assigneeId);
    }

    public int getTaskCountByCreator(Long creatorId) {
        return taskRepository.countByCreatorId(creatorId);
    }

    public int getCompletedTaskCountByUser(Long userId) {
        return taskRepository.countCompletedTasksByUser(userId,TaskStatus.COMPLETED);

    }

    public List<Task> getTasksByUserIdAndStatus(Long userId, TaskStatus status) {
        return taskRepository.findByAssigneeIdAndStatus(userId,status);
    }

    public void changeTaskStatus(Long taskId, TaskStatus newStatus) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User user = userDetailsService.getUserByUsername(username);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setUser(user);
        history.setTimestamp(LocalDateTime.now());
        history.setDescription("Статус изменен с " + oldStatus + " на " + newStatus);
        task.getHistory().add(history);

        taskRepository.save(task);
    }

    public List<TaskHistory> getTaskHistory(Long taskId, String sortOrder) {
        if ("asc".equalsIgnoreCase(sortOrder)) {
            return taskHistoryRepository.findByTaskIdOrderByTimestampAsc(taskId);
        } else {
            return taskHistoryRepository.findByTaskIdOrderByTimestampDesc(taskId);
        }
    }

    public List<TaskHistory> getLastTaskHistoryByUserId(Long userId) {
        return taskHistoryRepository.findByUserIdOrderByTimestamp(userId);
    }
}
