package com.disp.celesmaproject.model;

import com.disp.celesmaproject.util.AuthenticationFacade;
import com.disp.celesmaproject.repo.ProjectMemberRepository;
import com.disp.celesmaproject.repo.UserRepository;
import com.disp.celesmaproject.repo.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    // Сохранение проекта с установкой роли создателя как "ADMIN"
    public void saveProject(Project project) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        project.setOwnerUser(user); // Устанавливаем владельца проекта
        if (project.getMembers() == null) {
            project.setMembers(new HashSet<ProjectMember>());
        }
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(ProjectRole.ADMIN); // Устанавливаем роль "ADMIN" для создателя
        project.getMembers().add(member);
        projectRepository.save(project);
    }

    public void deleteProject(Long projectId) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Project project =  getProjectById(projectId);
        if(!user.equals(project.getOwnerUser())) {
            throw new IllegalStateException("User does not have permission to delete project. Only Owner can delete the project.");
        }
        projectRepository.deleteById(projectId);
    }


    public void updateProject(Project updatedProject, Long projectId) {
        Project project =  getProjectById(projectId);
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        projectRepository.save(project);
    };

    // Обновление роли участника в проекте
    public void updateMemberRole(Long projectId, Long memberId, ProjectRole role) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        ProjectMember projectMember = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found. UserId:" + memberId));
        Long userId = projectMember.getUser().getId();
        Long ownerId = project.getOwnerUser().getId();
        if(userId.equals(ownerId)) {
            throw new IllegalStateException("User is owner");
        }
        if(userId.equals(currentUser.getId())) {
            throw new IllegalStateException("User can't update myself");
        }
        project.updateMemberRole(projectMember, role); // Обновляем роль участника
        projectRepository.save(project);
    }

    public void deleteMember(Long projectId, Long memberId) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        ProjectMember projectMember = projectMemberRepository.findById(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found. UserId:" + memberId));
        Long userId = projectMember.getUser().getId();
        Long ownerId = project.getOwnerUser().getId();
        if(userId.equals(ownerId)) {
            throw new IllegalStateException("User is owner");
        }
        if(userId.equals(currentUser.getId())) {
            throw new IllegalStateException("User can't remove myself");
        }
        project.deleteMember(projectMember);
        projectRepository.save(project);

    }

    // Получение проектов, в которых участвует пользователь
    public List<Project> getProjectsByMember(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Находим ProjectMember по пользователю
        List<ProjectMember> projectMembers = projectMemberRepository.findByUser(user);

        // Собираем уникальные проекты из ProjectMember
        return projectMembers.stream()
                .map(ProjectMember::getProject)
                .distinct()
                .collect(Collectors.toList());
    }

    public int getUserProjectCount(Long userId) {
        return projectMemberRepository.countByUserId(userId);
    }

    // Получение проекта по ID с участниками
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    // Получение всех проектов
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Поиск проектов по названию или ID
    public List<Project> searchProjects(String query) {
        if (isNumeric(query)) {
            Long id = Long.parseLong(query);
            Optional<Project> project = projectRepository.findById(id);
            return project.map(List::of).orElse(List.of());
        } else {
            return projectRepository.findByNameContaining(query);
        }
    }

    // Проверка, является ли строка числом
    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isMember(List<User> projectUsers, User user) {
        return projectUsers.contains(user);
    }

    //Сортированный список по приоритету (ADMIN->MODERATOR->MEMBER)
    public List<ProjectMember> getSortedProjectMembers(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

        Map<ProjectRole, Integer> rolePriority = new HashMap<>();
        rolePriority.put(ProjectRole.ADMIN, 1); // Высший приоритет
        rolePriority.put(ProjectRole.MODERATOR, 2); // Средний приоритет
        rolePriority.put(ProjectRole.MEMBER, 3); // Низший приоритет

        // Сортируем участников по приоритету роли
        members.sort(Comparator.comparing(member -> rolePriority.getOrDefault(member.getRole(), Integer.MAX_VALUE)));

        return members;
    }

    //Перенесен из ProjectController. Конвертер типа: ProjectMember -> User
    public List<User> getConvertedProjectMembersToUsers(List<ProjectMember> members) {
        return members.stream().map(ProjectMember::getUser).toList();
    }

    //Добавляем пользователя в список желащтх вступить
    public void addJoinRequest(Project project, User user) {
        // Проверяем, не подал ли пользователь уже заявку и не числится ли пользователь в уже проекте
        List<User> alreadyMembers = project.getMembers().stream().map(ProjectMember::getUser).toList();
        if (!project.getApplicants().contains(user) && !alreadyMembers.contains(user)) {
            project.getApplicants().add(user);
            projectRepository.save(project);
        }
    }

    //Принять запрос на вступление
    public void approveApplicant(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Удаляем пользователя из списка заявок
        project.getApplicants().remove(user);

        // Добавляем пользователя в список участников
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(ProjectRole.MEMBER); //Стандартная роль.
        project.getMembers().add(member);

        projectRepository.save(project);
    }

    //Отказать в запрое на вступление
    public void rejectApplicant(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Удаляем пользователя из списка заявок
        project.getApplicants().remove(user);
        projectRepository.save(project);
    }

}

