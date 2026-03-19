package com.wanderaTech.order_service.Controller;

import com.wanderaTech.order_service.OrderDto.OrderRequest;
import com.wanderaTech.order_service.OrderDto.OrderResponse;
import com.wanderaTech.order_service.Service.OrderServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderServiceImplementation orderServiceImplementation;

    @PostMapping("/place")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderServiceImplementation.placeOrder(orderRequest);
    }
    @GetMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrdersByCustomer(@PathVariable String customerId) {
        return orderServiceImplementation.getOrdersByCustomer(customerId);
    }
}
