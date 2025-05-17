package com.franquicias.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateStockRequest {

    @Min(0)
    private int stock;   // new stock value
}
