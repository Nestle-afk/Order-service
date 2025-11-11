package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUserId(Long id);

    List<Order> findAllByStatusIn(List<OrderStatus> statuses);
}
