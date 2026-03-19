package com.wanderaTech.order_service.OrderDto;

import com.wanderaTech.order_service.Enum.PaymentMethod;
import com.wanderaTech.order_service.Model.Address;
import com.wanderaTech.order_service.Model.DeliveryDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  OrderRequest {
     private String customerId;
    private PaymentMethod paymentMethod;
    private Address deliveryAddress;
    private DeliveryDetails deliveryDetails;
    private  String phoneNumber;

}
