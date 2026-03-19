package com.wanderaTech.order_service.Repository;

import com.wanderaTech.order_service.UsersReplicaModel.CustomerSnapSot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CustomerSnapshotRepository extends JpaRepository<CustomerSnapSot,Long> {
    Optional<CustomerSnapSot> findByCustomerId(String customerId);
}
