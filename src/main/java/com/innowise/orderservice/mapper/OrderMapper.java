package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderDto;
import com.innowise.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "user", ignore = true)
    OrderDto toDto(Order entity);

    @Mapping(target = "userId", source = "user.id")
    Order toEntity(OrderDto dto);
}
