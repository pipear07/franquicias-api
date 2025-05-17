package com.franquicias.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddBranchRequest {
    @NotBlank
    private String id;          // branch identifier
    @NotBlank
    private String name;
}

