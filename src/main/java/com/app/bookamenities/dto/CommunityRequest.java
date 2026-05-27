package com.app.bookamenities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommunityRequest {
    
    @NotBlank(message = "Community name is required")
    private String communityName;
    @NotBlank(message = "Address is required field")
    private String address;
    @NotBlank(message = "Community type is required field")
    private String type;
    @NotBlank(message = "Total flats count is required field")
    private String flatsCount;

}
