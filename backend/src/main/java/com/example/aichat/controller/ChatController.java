package com.example.aichat.controller;

import com.example.aichat.dto.ApiResponse;
import com.example.aichat.dto.ChatMessageRequest;
import com.example.aichat.dto.ConversationCreateRequest;
import com.example.aichat.entity.Conversation;
import com.example.aichat.entity.Message;
import com.example.aichat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 标注为 REST 控制器
@RequestMapping("/api") // 定义基础路径
@RequiredArgsConstructor // 自动生成构造函数
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ApiResponse<List<Conversation>> listConversations() {
        // 获取用户的所有会话列表
        Long userId = 1L;
        return ApiResponse.success(chatService.listConversations(userId));
    }

    @PostMapping("/conversations")
    public ApiResponse<Conversation> createConversation(@Valid @RequestBody ConversationCreateRequest body) {
        // 创建一个新的会话
        Long userId = 1L;
        return ApiResponse.success(chatService.createConversation(userId, body));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<List<Message>> listMessages(@PathVariable("id") Long id) {
        // 获取指定会话的所有消息
        Long userId = 1L;
        return ApiResponse.success(chatService.listMessages(userId, id));
    }

    @PostMapping("/chat/{conversationId}")
    public ApiResponse<Message> sendMessage(@PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageRequest body) {
        // 向指定会话发送消息
        Long userId = 1L;
        return ApiResponse.success(chatService.sendMessage(userId, conversationId, body));
    }

    @PostMapping(value = "/chat/{conversationId}/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public reactor.core.publisher.Flux<String> streamMessage(@PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageRequest body) {
        Long userId = 1L;
        return chatService.streamMessage(userId, conversationId, body);
    }
}
