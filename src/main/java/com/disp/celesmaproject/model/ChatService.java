package com.disp.celesmaproject.model;

import com.disp.celesmaproject.repo.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessagesByProject(Long projectId) {
        return chatMessageRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }
}