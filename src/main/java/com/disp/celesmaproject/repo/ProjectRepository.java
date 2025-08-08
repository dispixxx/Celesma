package com.disp.celesmaproject.repo;

import com.disp.celesmaproject.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByNameContaining(String name); // Поиск по названию
    Optional<Project> findById(Long id); // Поиск по ID
}
