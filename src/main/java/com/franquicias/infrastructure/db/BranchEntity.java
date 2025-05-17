package com.franquicias.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchEntity {
    private String id;
    private String name;
    private List<ProductEntity> products;
}
