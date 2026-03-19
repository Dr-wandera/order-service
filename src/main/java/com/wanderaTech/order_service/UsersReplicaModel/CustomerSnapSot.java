package com.wanderaTech.order_service.UsersReplicaModel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSnapSot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String customerId;
    private String email;
    private String firstName;
    private String phoneNumber;
}
