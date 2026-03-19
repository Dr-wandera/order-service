package com.wanderaTech.order_service.Repository;

import com.wanderaTech.order_service.UsersReplicaModel.SellerSnapShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerSnapShotRepository extends JpaRepository<SellerSnapShot, Long> {
   Optional<SellerSnapShot> findBySellerId(String sellerId);
}
