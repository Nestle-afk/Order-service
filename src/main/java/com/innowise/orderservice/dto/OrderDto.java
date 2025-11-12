package com.innowise.orderservice.dto;

import com.innowise.orderservice.model.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long id;

    @NotNull(message = "Status cannot be blank")
    private OrderStatus status;

    @PositiveOrZero(message = "Total amount must be positive or zero")
    private Double totalAmount;

    @NotEmpty(message = "Order must contain at least one item")
    private List<ItemDto> items;

    @With
    private UserDto user;
}
