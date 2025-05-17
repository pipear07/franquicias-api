package com.franquicias.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;         

@Data
@Document(collection = "franchises") // collection name
@AllArgsConstructor     // It is added to avoid problems in unit tests.
@NoArgsConstructor      // It is added to avoid problems in unit tests.
public class FranchiseEntity {
    @Id
    private String id;
    private String name;

    private List<BranchEntity> branches;   //  new field
}
