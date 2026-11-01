package com.soumadeep.SpringEcommerce.Service;

import com.soumadeep.SpringEcommerce.Enum.TokenAction;
import com.soumadeep.SpringEcommerce.Repo.AppUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final AppUserRepo appUserRepo;

    @Value("${smartcart.tokens.cost.chat}")
    private int chatCost;

    @Value("${smartcart.tokens.cost.description}")
    private int descriptionCost;

    @Value("${smartcart.tokens.cost.image}")
    private int imageCost;

    @Value("${smartcart.admin.email}")
    private String adminEmail;

    public <T> T charge(String email, TokenAction action, Supplier<T> aiCall) {
        boolean admin = email.equalsIgnoreCase(adminEmail);
        int cost = costOf(action);

        if (!admin && appUserRepo.deductTokens(email, cost) == 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Insufficient tokens");
        }

        try {
            return aiCall.get();
        }
        catch (ResponseStatusException e) {
            if(!admin)
                appUserRepo.addTokens(email, cost);
            throw e;
        }
        catch (Exception e) {
            log.error("AI call failed for {}", action, e);
            if(!admin)
                appUserRepo.addTokens(email, cost);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI service unavailable");
        }
    }

    public int getBalance(String email) {
        return appUserRepo.findTokenBalance(email).orElse(0);
    }

    public Map<String, Integer> getCosts() {
        return Map.of("chat", chatCost, "description", descriptionCost, "image", imageCost);
    }

    private int costOf(TokenAction action) {
        return switch (action) {
            case CHAT -> chatCost;
            case DESCRIPTION -> descriptionCost;
            case IMAGE -> imageCost;
        };
    }
}