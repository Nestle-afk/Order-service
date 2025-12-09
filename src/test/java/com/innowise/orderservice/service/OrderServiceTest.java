package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.dto.OrderDto;
import com.innowise.orderservice.dto.UserDto;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserClient userClient;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDto orderDto;
    private UserDto userDto;

    @BeforeEach
    void setup() {
        userDto = new UserDto();
        order = new Order();
        order.setId(1L);
        order.setUserId(100L);
        order.setStatus(OrderStatus.IN_PROGRESS);

        orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setStatus(OrderStatus.IN_PROGRESS);
    }

    @Test
    void getOrder_shouldReturnOrderDto_whenOrderExists() {
        when(orderRepository.findByUserId(100L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClient.getUserById(100L)).thenReturn(userDto);

        OrderDto result = orderService.getOrder(100L);

        assertNotNull(result);
        verify(orderRepository).findByUserId(100L);
        verify(orderMapper).toDto(order);
        verify(userClient).getUserById(100L);
    }

    @Test
    void getOrder_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> orderService.getOrder(999L));
    }

    @Test
    void getOrdersByStatuses_shouldReturnListOfOrderDtos() {
        List<OrderStatus> statuses = List.of(OrderStatus.IN_PROGRESS, OrderStatus.COMPLETED);
        when(orderRepository.findAllByStatusIn(statuses)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClient.getUserById(100L)).thenReturn(userDto);

        List<OrderDto> result = orderService.getOrdersByStatuses(statuses);

        assertEquals(1, result.size());
        verify(orderRepository).findAllByStatusIn(statuses);
    }

    @Test
    void createOrder_shouldSaveAndReturnOrderDto() {
        when(orderMapper.toEntity(orderDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClient.getUserById(100L)).thenReturn(userDto);

        OrderDto result = orderService.createOrder(orderDto);

        assertNotNull(result);
        verify(orderRepository).save(order);
        verify(orderMapper).toEntity(orderDto);
    }

    @Test
    void updateOrder_shouldUpdateStatusAndItems() {
        order.setOrderItems(new ArrayList<>());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemMapper.toEntityList(any(), eq(order))).thenReturn(List.of());
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClient.getUserById(100L)).thenReturn(userDto);

        orderDto.setStatus(OrderStatus.COMPLETED);

        OrderDto result = orderService.updateOrder(1L, orderDto);

        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrder_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> orderService.updateOrder(1L, orderDto));
    }

    @Test
    void deleteOrder_shouldDelete_whenExists() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        orderService.deleteOrder(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteOrder_shouldThrowException_whenNotFound() {
        when(orderRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> orderService.deleteOrder(1L));
    }
}
