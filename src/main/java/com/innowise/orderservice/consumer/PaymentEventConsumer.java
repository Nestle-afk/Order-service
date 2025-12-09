package com.innowise.orderservice.consumer;

import com.innowise.orderservice.dto.CreatePaymentEvent;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "create-payment-topic", groupId = "order-service-group",
                   containerFactory = "paymentEventKafkaListenerContainerFactory")
    @Transactional
    public void handleCreatePaymentEvent(CreatePaymentEvent event) {
        log.info("Received CREATE_PAYMENT event for order ID: {}, status: {}", 
                event.getOrderId(), event.getStatus());

        try {
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
                order.setStatus(OrderStatus.COMPLETED);
            } else if ("FAILED".equalsIgnoreCase(event.getStatus())) {
                order.setStatus(OrderStatus.FAILED);
            }

            orderRepository.save(order);
            log.info("Order status updated for order ID: {} to status: {}", 
                    event.getOrderId(), order.getStatus());
        } catch (Exception e) {
            log.error("Failed to process CREATE_PAYMENT event for order ID: {}", 
                    event.getOrderId(), e);
        }
    }
}

