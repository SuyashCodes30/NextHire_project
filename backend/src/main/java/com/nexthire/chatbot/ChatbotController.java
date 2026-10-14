package com.nexthire.chatbot;

import com.nexthire.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "Platform navigation and guidance chatbot endpoints")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    @Operation(summary = "Send message to NextHire Assistant", description = "Ask questions regarding platform usage, interview tracks, scoring, or English learning")
    public ResponseEntity<ApiResponse<ChatbotResponse>> sendMessage(@Valid @RequestBody ChatbotRequest request) {
        ChatbotResponse response = chatbotService.processMessage(request);
        return ResponseEntity.ok(ApiResponse.ok("Assistant responded.", response));
    }
}
