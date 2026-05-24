package com.wanderaTech.order_service.Controller;

import com.wanderaTech.order_service.OrderDto.OrderRequest;
import com.wanderaTech.order_service.OrderDto.OrderResponse;
import com.wanderaTech.order_service.Service.OrderServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusimport org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderServiceImplementation orderServiceImplementation;

    @PostMapping("/place")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody OrderRequest orderRequest
    ) {
        return orderServiceImplementation.placeOrder(userId,orderRequest);
    }
    // orders customer placed
    @GetMapping("/customer/order")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrdersByCustomer(@RequestHeader("X-User-Id") String userId) {
        return orderServiceImplementation.getOrdersByCustomer(userId);
    }

    @GetMapping("/getOrdersByDate")
    public List<OrderResponse> getAllOrdersUnderDate(
            @RequestParam LocalDate orderDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ){
        return orderServiceImplementation.getAllOrdersUnderDate(orderDate,size,page);
    }

}
