package com.wanderaTech.order_service.Service;

import com.wanderaTech.order_service.OrderDto.OrderRequest;
import com.wanderaTech.order_service.OrderDto.OrderResponse;

import java.util.List;

public interface OrderServiceInterface {
    OrderResponse placeOrder(OrderRequest orderRequest);

    List<OrderResponse> getOrdersByCustomer(String customerId);
}
