package com.wanderaTech.order_service.OrderDto;

import com.wanderaTech.order_service.Enum.PaymentMethod;
import com.wanderaTech.order_service.Model.Address;
import com.wanderaTech.order_service.Model.DeliveryDetails;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  OrderRequest {
    @NotBlank(message = "field required")
    private PaymentMethod paymentMethod;
    @NotBlank(message = "field required")
    private Address deliveryAddress;
    @NotBlank(message = "field required")
    private DeliveryDetails deliveryDetails;
    @NotBlank(message = "field required")
    private  String phoneNumber;

}
