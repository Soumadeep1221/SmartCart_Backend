package com.soumadeep.SpringEcommerce.Controller;

import com.soumadeep.SpringEcommerce.DTO.AuthResponse;
import com.soumadeep.SpringEcommerce.DTO.GoogleLoginRequest;
import com.soumadeep.SpringEcommerce.DTO.UserResponse;
import com.soumadeep.SpringEcommerce.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.token()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }
}