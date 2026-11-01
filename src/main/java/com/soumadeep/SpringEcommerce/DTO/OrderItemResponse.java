package com.soumadeep.SpringEcommerce.DTO;

import java.math.BigDecimal;

public record OrderItemResponse(String productName, int quantity, BigDecimal totalPrice) { }
