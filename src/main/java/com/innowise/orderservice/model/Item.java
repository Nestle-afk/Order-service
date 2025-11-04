package com.innowise.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "items",
        indexes = {
                @Index(name = "idx_items_name", columnList = "name", unique = true)
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private double price;
}
