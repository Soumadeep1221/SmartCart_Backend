package com.soumadeep.SpringEcommerce.DTO;

public record UserResponse(String name, String email, String pictureUrl,
                           int tokenBalance, boolean admin) {
}
