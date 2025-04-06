package com.disp.celesmaproject.repo;

import com.disp.celesmaproject.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
/*    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.members WHERE p.id = :id")
    Optional<Project> findByIdWithMembers(@Param("id") Long id);
    List<Project> findByMembersContaining(User user);
    @Query("SELECT p FROM Project p JOIN p.members pm WHERE pm = :projectMember")
    List<Project> findByMembersContaining(@Param("projectMember") ProjectMember projectMember);*/
    //@Query("SELECT p FROM Project p WHERE p.name LIKE %:name%")
    List<Project> findByNameContaining(String name); // Поиск по названию
    Optional<Project> findById(Long id); // Поиск по ID
}
