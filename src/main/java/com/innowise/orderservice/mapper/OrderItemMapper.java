package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.ItemDto;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", expression = "java(order)")
    @Mapping(target = "item", source = "dto")
    @Mapping(target = "quantity", source = "dto.quantity")
    OrderItem toEntity(ItemDto dto, @Context Order order);

    @IterableMapping(qualifiedByName = "toEntityWithOrder")
    List<OrderItem> toEntityList(List<ItemDto> dtos, @Context Order order);

    @Named("toEntityWithOrder")
    default OrderItem mapWithOrder(ItemDto dto, @Context Order order) {
        return toEntity(dto, order);
    }
}
