package com.innowise.orderservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.orderservice.dto.ItemDto;
import com.innowise.orderservice.dto.OrderDto;
import com.innowise.orderservice.dto.UserDto;
import com.innowise.orderservice.model.Item;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("orders_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgreSQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgreSQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> postgreSQLContainer.getPassword());

        registry.add("user-service.url", () -> "http://localhost:8089/users");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long existingItemId;

    private WireMockServer wireMockServer;

    @BeforeEach
    void startWireMockAndCleanDb() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(8089);
            wireMockServer.start();
        }
        wireMockServer.resetAll();
        wireMockServer.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get(
                                com.github.tomakehurst.wiremock.client.WireMock.urlMatching("/users/.*"))
                        .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"id\":1,\"email\":\"user@test.com\",\"name\":\"Test User\"}"))
        );

        orderRepository.deleteAll();
        itemRepository.deleteAll();

        Item item = new Item();
        item.setName("Item 1");
        item.setPrice(100.0);
        itemRepository.save(item);

        existingItemId = item.getId();
    }

    @Test
    void testCreateGetUpdateDeleteOrder() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.IN_PROGRESS);
        orderDto.setTotalAmount(100.0);

        ItemDto item = new ItemDto();
        item.setId(existingItemId);
        item.setName("Item 1");
        item.setPrice(100.0);
        item.setQuantity(1);
        orderDto.setItems(List.of(item));

        UserDto user = new UserDto();
        user.setId(1L);
        user.setEmail("user@test.com");
        orderDto.setUser(user);

        String createContent = objectMapper.writeValueAsString(orderDto);

        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.user.email").value("user@test.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto createdOrder = objectMapper.readValue(response, OrderDto.class);
        Long orderId = createdOrder.getId();

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        mockMvc.perform(get("/orders").param("statuses", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId));

        createdOrder.setItems(List.of(item));
        createdOrder.setStatus(OrderStatus.COMPLETED);

        String updateContent = objectMapper.writeValueAsString(createdOrder);

        mockMvc.perform(put("/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(delete("/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }
}
