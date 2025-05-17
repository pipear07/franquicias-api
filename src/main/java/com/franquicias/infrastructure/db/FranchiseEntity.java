package com.franquicias.infrastructure.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;         //  nuevo import

@Data
@Document(collection = "franchises") // collection name
public class FranchiseEntity {
    @Id
    private String id;
    private String name;

    private List<BranchEntity> branches;   //  nuevo campo
}
