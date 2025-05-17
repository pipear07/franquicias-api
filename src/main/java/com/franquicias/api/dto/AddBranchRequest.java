package com.franquicias.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBranchRequest {
    @NotBlank
    private String id;          // branch identifier
    @NotBlank
    private String name;
}

