package com.soumadeep.SpringEcommerce.Controller;

import com.soumadeep.SpringEcommerce.DTO.OrderRequest;
import com.soumadeep.SpringEcommerce.DTO.OrderResponse;
import com.soumadeep.SpringEcommerce.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("orders/place")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest orderRequest, Authentication authentication)
    {
        OrderResponse orderResponse = orderService.placeOrder(orderRequest, authentication.getName());
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders()
    {
        List<OrderResponse> responses=orderService.getAllOrderResponses();
        return new ResponseEntity<>(responses,HttpStatus.OK);
    }
}
