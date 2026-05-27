package com.app.bookamenities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomChatRequest {

    @NotBlank(message = "user Id should not be blank")
    private String userId;

    @NotBlank(message = "Query should not be blank")
    private String query;
}