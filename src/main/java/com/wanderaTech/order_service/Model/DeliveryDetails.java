package com.wanderaTech.order_service.Model;

import jakarta.persistence.Embeddable;

@Embeddable
public record DeliveryDetails(
        String pickUpStation,
        String doorDelivery
) {
}
