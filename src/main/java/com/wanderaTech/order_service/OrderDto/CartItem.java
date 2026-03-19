package com.wanderaTech.order_service.OrderDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    private String productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private String sellerId;
}
