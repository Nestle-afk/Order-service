package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.dto.CreateOrderEvent;
import com.innowise.orderservice.dto.OrderDto;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderItem;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    public final UserClient userClient;
    public final OrderRepository repository;
    public final OrderMapper orderMapper;
    public final OrderItemMapper orderItemMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String CREATE_ORDER_TOPIC = "create-order-topic";

    public OrderService(UserClient userClient, OrderRepository repository, OrderMapper orderMapper, 
                       OrderItemMapper orderItemMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.userClient = userClient;
        this.repository = repository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.kafkaTemplate = kafkaTemplate;
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

        Double totalAmount = dto.getTotalAmount();
        if (totalAmount == null && dto.getItems() != null) {
            totalAmount = dto.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }

        Order saved = repository.save(order);
        OrderDto savedDto = orderMapper.toDto(saved)
                .withUser(userClient.getUserById(saved.getUserId()));

        CreateOrderEvent event = new CreateOrderEvent(
                saved.getId(),
                saved.getUserId(),
                totalAmount != null ? totalAmount : 0.0
        );

        try {
            kafkaTemplate.send(CREATE_ORDER_TOPIC, event);
            log.info("CREATE_ORDER event sent for order ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("Failed to send CREATE_ORDER event for order ID: {}", saved.getId(), e);
        }

        return savedDto;
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
