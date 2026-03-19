package com.wanderaTech.order_service.OrderDto;

import com.wanderaTech.order_service.Model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderNumber;
    private Double totalAmount;
    private List<OrderItem> items;
}
