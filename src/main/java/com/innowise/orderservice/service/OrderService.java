package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.dto.OrderDto;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderItem;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public final UserClient userClient;
    public final OrderRepository repository;
    public final OrderMapper orderMapper;

    public final OrderItemMapper orderItemMapper;

    public OrderService(UserClient userClient, OrderRepository repository, OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.userClient = userClient;
        this.repository = repository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public OrderDto getOrder(Long id){
        Order order = repository.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return orderMapper.toDto(order).withUser(userClient.getUserById(id));
    }

    public List<OrderDto> getOrdersByStatuses(List<OrderStatus> statuses) {
        List<Order> orders = repository.findAllByStatusIn(statuses);
        return orders.stream()
                .map(order -> orderMapper.toDto(order)
                        .withUser(userClient.getUserById(order.getUserId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto createOrder(OrderDto dto) {
        Order order = orderMapper.toEntity(dto);
        order.setCreationDate(LocalDateTime.now());

        Order saved = repository.save(order);
        return orderMapper.toDto(saved)
                .withUser(userClient.getUserById(saved.getUserId()));
    }

    @Transactional
    public OrderDto updateOrder(Long id, OrderDto dto) {
        Order current = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        current.setStatus(dto.getStatus());
        List<OrderItem> newItems = orderItemMapper.toEntityList(dto.getItems(), current);
        current.getOrderItems().addAll(newItems);

        Order updated = repository.save(current);
        return orderMapper.toDto(updated)
                .withUser(userClient.getUserById(updated.getUserId()));
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        repository.deleteById(id);
    }
}
