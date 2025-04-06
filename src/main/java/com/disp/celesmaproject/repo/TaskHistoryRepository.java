package com.disp.celesmaproject.repo;

import com.disp.celesmaproject.model.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    @Query("SELECT h FROM TaskHistory h WHERE h.task.id = :taskId ORDER BY h.timestamp DESC")
    List<TaskHistory> findByTaskIdOrderByTimestampDesc(@Param("taskId") Long taskId);

    @Query("SELECT h FROM TaskHistory h WHERE h.task.id = :taskId ORDER BY h.timestamp ASC")
    List<TaskHistory> findByTaskIdOrderByTimestampAsc(@Param("taskId") Long taskId);
}