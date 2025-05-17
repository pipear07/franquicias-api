package com.franquicias.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddProductRequest {
    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @Min(0)
    private int stock;
}
