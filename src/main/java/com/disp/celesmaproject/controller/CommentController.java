package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.model.*;
import com.disp.celesmaproject.util.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/tasks/{taskId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AuthenticationFacade authenticationFacade;

    @GetMapping
    @ResponseBody
    public List<Comment> getComments(@PathVariable Long taskId) {
        return commentService.getCommentsByTaskId(taskId);
    }

    @PostMapping
    public String addComment(@PathVariable Long projectId, @PathVariable Long taskId,
                             @RequestParam String commentText,
                             @AuthenticationPrincipal UserDetails userDetails) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User user = userDetailsService.getUserByUsername(username);
        Task task = taskService.getTaskById(taskId);
        Comment comment = new Comment();
        comment.setText(commentText);
        comment.setTask(task);
        comment.setAuthor(user);
        commentService.addComment(comment);
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }
}