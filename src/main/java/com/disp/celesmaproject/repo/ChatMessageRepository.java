package com.disp.celesmaproject.repo;

import com.disp.celesmaproject.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByProjectIdOrderByTimestampAsc(Long projectId);
}
