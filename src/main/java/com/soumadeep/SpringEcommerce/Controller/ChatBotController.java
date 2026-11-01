package com.soumadeep.SpringEcommerce.Controller;

import com.soumadeep.SpringEcommerce.Enum.TokenAction;
import com.soumadeep.SpringEcommerce.Service.ChatBotService;
import com.soumadeep.SpringEcommerce.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;
    private final TokenService tokenService;

    @GetMapping("/ask")
    public ResponseEntity<String> chatBot(@RequestParam String message, Authentication authentication) {
        String email = authentication.getName();
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String response = tokenService.charge(email, TokenAction.CHAT,
                () -> chatBotService.getBotResponse(message, email, admin));
        return ResponseEntity.ok()
                .header("X-Token-Balance", String.valueOf(tokenService.getBalance(email)))
                .body(response);
    }
}
