package com.wanderaTech.order_service.Repository;

import com.wanderaTech.order_service.UsersReplicaModel.UsersSnapShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserSnapShotRepository extends JpaRepository<UsersSnapShot, Long> {
    Optional<UsersSnapShot> findByUserId(String userId);
}
