package com.soumadeep.SpringEcommerce.DTO;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String token) {}

