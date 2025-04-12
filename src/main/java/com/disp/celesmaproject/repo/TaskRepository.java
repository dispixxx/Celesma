package com.disp.celesmaproject.repo;


import com.disp.celesmaproject.model.Task;
import com.disp.celesmaproject.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssigneeIdAndProjectId(Long assigneeId, Long projectId);
    List<Task> findByCreatorId(Long creatorId);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByCreatorIdAndProjectId(Long assignee_id, Long project_id);
    int countByAssigneeId(Long assignee_id);
    int countByCreatorId(Long creatorId);
    @Query("SELECT COUNT(DISTINCT t.id) FROM Task t WHERE (t.assignee.id = :userId OR t.creator.id = :userId) AND t.status = 'COMPLETED'")
    int countCompletedTasksByUser(@Param("userId") Long userId, TaskStatus status);

    List<Task> findByAssigneeIdAndStatus(Long userId, TaskStatus status);
}