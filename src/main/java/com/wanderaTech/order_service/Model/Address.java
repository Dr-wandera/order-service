package com.wanderaTech.order_service.Model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String county,
        String constituency,
        String nearestTown,
        String phoneNumber,
        String houseNumber
) {
}
