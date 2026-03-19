package com.wanderaTech.order_service.UsersReplicaModel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class SellerSnapShot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String sellerId;
    private String email;
    private String firstName;
}
