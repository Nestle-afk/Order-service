package com.innowise.orderservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ItemDto {
    private Long id;

    @NotBlank(message = "Item name cannot be blank")
    private String name;

    @Positive(message = "Price must be positive")
    private Double price;

    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Integer quantity;
}
