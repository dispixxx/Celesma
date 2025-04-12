package com.disp.celesmaproject.repo;

import com.disp.celesmaproject.model.ProjectMember;
import com.disp.celesmaproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByUser(User user);
    List<ProjectMember> findByProjectId(Long projectId);
    Optional<ProjectMember> findByUserAndProjectId(User user, Long projectId);
    int countByUserId(Long id);
}