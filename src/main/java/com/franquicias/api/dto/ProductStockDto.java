package com.franquicias.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductStockDto {
    private String productId;
    private String productName;
    private String branchId;
    private String branchName;
    private int stock;
}

