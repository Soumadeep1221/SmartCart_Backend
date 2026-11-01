package com.soumadeep.SpringEcommerce.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.soumadeep.SpringEcommerce.DTO.AuthResponse;
import com.soumadeep.SpringEcommerce.DTO.UserResponse;
import com.soumadeep.SpringEcommerce.Entity.AppUser;
import com.soumadeep.SpringEcommerce.Repo.AppUserRepo;
import com.soumadeep.SpringEcommerce.Util.GoogleAuthUtil;
import com.soumadeep.SpringEcommerce.Util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleAuthUtil googleAuthUtil;
    private final JwtUtil jwtUtil;
    private final AppUserRepo appUserRepo;

    @Value("${smartcart.tokens.initial}")
    private int initialTokens;

    @Value("${smartcart.admin.email}")
    private String adminEmail;

    @Transactional
    public AuthResponse googleLogin(String idToken) {
        GoogleIdToken.Payload payload;
        try {
            payload = googleAuthUtil.verify(idToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google sign-in");
        }

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email not verified");
        }

        String email = payload.getEmail();

        AppUser user = appUserRepo.findByEmail(email).orElseGet(() -> {
            AppUser newUser = new AppUser();
            newUser.setEmail(email);
            newUser.setTokenBalance(initialTokens);
            return newUser;
        });
        user.setName((String) payload.get("name"));
        user.setPictureUrl((String) payload.get("picture"));
        appUserRepo.save(user);

        return new AuthResponse(jwtUtil.generateToken(email), toResponse(user));
    }

    public UserResponse getCurrentUser(String email) {
        AppUser user = appUserRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return toResponse(user);
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getName(), user.getEmail(), user.getPictureUrl(), user.getTokenBalance(),
                user.getEmail().equalsIgnoreCase(adminEmail));
    }
}