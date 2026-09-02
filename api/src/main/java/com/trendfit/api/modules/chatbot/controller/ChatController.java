package com.trendfit.api.modules.chatbot.controller;

import com.trendfit.api.modules.chatbot.dto.ChatErrorResponse;
import com.trendfit.api.modules.chatbot.dto.ChatRequest;
import com.trendfit.api.modules.chatbot.dto.ChatResponse;
import com.trendfit.api.modules.chatbot.service.ChatException;
import com.trendfit.api.modules.chatbot.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ChatController {

    private static final int MAX_MESSAGE_LENGTH = 2_000;

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody(required = false) ChatRequest request) {
        String message = request == null || request.message() == null
                ? ""
                : request.message().trim();

        if (message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatErrorResponse("Vui lòng nhập câu hỏi."));
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ResponseEntity.badRequest()
                    .body(new ChatErrorResponse("Câu hỏi không được vượt quá 2.000 ký tự."));
        }

        try {
            return ResponseEntity.ok(new ChatResponse(chatService.ask(message)));
        } catch (ChatException exception) {
            return ResponseEntity.status(exception.getStatus())
                    .body(new ChatErrorResponse(exception.getMessage()));
        }
    }
}
