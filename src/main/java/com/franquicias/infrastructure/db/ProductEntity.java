package com.franquicias.infrastructure.db;

import lombok.Data;

@Data
public class ProductEntity {
    private String id;
    private String name;
    private int stock;
}
