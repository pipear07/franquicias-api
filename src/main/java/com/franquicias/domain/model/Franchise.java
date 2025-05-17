package com.franquicias.domain.model;

import lombok.Data;
import java.util.List;

@Data
public class Franchise {
    private String id;
    private String name;
    private List<Branch> branches;
}

