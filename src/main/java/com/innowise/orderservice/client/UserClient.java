package com.innowise.orderservice.client;

import com.innowise.orderservice.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    private static final String USER_SERVICE_CB = "userServiceCircuitBreaker";

    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "getDefaultUser")
    public UserDto getUserById(Long userId) {
        String url = String.format("%s/%d", userServiceUrl, userId);
        return restTemplate.getForObject(url, UserDto.class);
    }

    public UserDto getDefaultUser(Long userId, Throwable throwable) {
        log.warn(
                "request failed with error ${}", throwable.getMessage());
        return UserDto.builder()
                .id(userId)
                .name("Unknown")
                .surname("User")
                .birthDate(LocalDate.of(2010,10,10))
                .email("unavailable@user.com")
                .build();
    }
}

