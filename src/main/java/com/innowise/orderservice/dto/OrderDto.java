package com.innowise.orderservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class OrderDto {
    private Long id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Status cannot be blank")
    private String status;

    @PositiveOrZero(message = "Total amount must be positive or zero")
    private Double totalAmount;

    @NotEmpty(message = "Order must contain at least one item")
    private List<ItemDto> items;

    private UserDto user;
}
