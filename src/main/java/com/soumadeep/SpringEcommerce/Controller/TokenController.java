package com.soumadeep.SpringEcommerce.Controller;

import com.soumadeep.SpringEcommerce.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @GetMapping("/tokens/costs")
    public Map<String, Integer> costs() {
        return tokenService.getCosts();
    }
}