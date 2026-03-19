package com.wanderaTech.order_service.Repository;

import com.wanderaTech.order_service.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    boolean existsByOrderNumber(String orderNumber);

      List<Order> findAllByCustomerId(String customerId);

    Optional<Order> findByOrderNumber(String orderNumber);
}
