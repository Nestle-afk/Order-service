package com.innowise.orderservice.dto;

import java.time.LocalDate;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String surname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String email;
}


