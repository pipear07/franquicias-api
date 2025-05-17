package com.franquicias.infrastructure.db;

import lombok.Data;
import java.util.List;

@Data
public class BranchEntity {
    private String id;
    private String name;
    private List<ProductEntity> products;
}
