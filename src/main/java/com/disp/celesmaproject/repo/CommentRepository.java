package com.disp.celesmaproject.repo;

import com.disp.celesmaproject.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskId(Long taskId);
    //Поиск свежуго комментария
//    @Query("SELECT h FROM TaskHistory h WHERE h.task.id = :taskId ORDER BY h.timestamp ASC")
    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId ORDER BY c.createdAt DESC LIMIT 1")
    Optional<Comment> findTop1ByAuthorIdOrderByCreatedAtDesc(@Param("userId")Long userId);
}