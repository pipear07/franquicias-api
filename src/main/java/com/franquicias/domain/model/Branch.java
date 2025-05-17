package com.franquicias.domain.model;

import lombok.Data;
import java.util.List;

@Data
public class Branch {
    private String id;
    private String name;
    private List<Product> products;
}
