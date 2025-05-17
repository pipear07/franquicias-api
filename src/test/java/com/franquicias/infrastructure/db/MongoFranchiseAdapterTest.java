package com.franquicias.infrastructure.db;

import com.franquicias.domain.model.Franchise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class MongoFranchiseAdapterTest {

    private ReactiveMongoTemplate template;
    private ModelMapper mapper;
    private MongoFranchiseAdapter adapter;

    @BeforeEach
    void setUp() {
        template = mock(ReactiveMongoTemplate.class);
        mapper   = new ModelMapper();
        adapter  = new MongoFranchiseAdapter(template, mapper);
    }

    @Test
    void save_shouldPersistEntityAndReturnDomain() {

        FranchiseEntity savedEntity = new FranchiseEntity();
        savedEntity.setId("F1");
        savedEntity.setName("Fr1");
        savedEntity.setBranches(List.of(
            new BranchEntity("B1","S1", List.of(
                new ProductEntity("P1","X", 3)
            ))
        ));

  
        given(template.save(any(FranchiseEntity.class)))
            .willReturn(Mono.just(savedEntity));


        Mono<Franchise> result = adapter.save(
            new Franchise("IGNORED","IGNORED", null)
        );

        StepVerifier.create(result)
                    .expectNextMatches(domain -> 
                        domain.getId().equals("F1")
                        && domain.getName().equals("Fr1")
                        && domain.getBranches().size() == 1
                        && domain.getBranches()
                                 .get(0)
                                 .getProducts()
                                 .get(0)
                                 .getStock() == 3
                    )
                    .verifyComplete();
    }

    @Test
    void findById_whenFound_shouldReturnDomain() {
        FranchiseEntity ent = new FranchiseEntity("F2","Fr2",
            List.of(new BranchEntity("B2","S2", List.of()))
        );

        given(template.findById("F2", FranchiseEntity.class))
            .willReturn(Mono.just(ent));

        StepVerifier.create(adapter.findById("F2"))
                    .expectNextMatches(f ->
                        f.getId().equals("F2")
                        && f.getName().equals("Fr2")
                        && f.getBranches().size() == 1
                    )
                    .verifyComplete();
    }

    @Test
    void findById_whenNotFound_shouldCompleteEmpty() {
        given(template.findById("NOPE", FranchiseEntity.class))
            .willReturn(Mono.empty());

        StepVerifier.create(adapter.findById("NOPE"))
                    .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllMapped() {
        FranchiseEntity e1 = new FranchiseEntity("A1","Name1", List.of());
        FranchiseEntity e2 = new FranchiseEntity("A2","Name2", List.of());
        given(template.findAll(FranchiseEntity.class))
            .willReturn(Flux.just(e1,e2));

        StepVerifier.create(adapter.findAll())
                    .expectNextMatches(f -> f.getId().equals("A1") && f.getName().equals("Name1"))
                    .expectNextMatches(f -> f.getId().equals("A2") && f.getName().equals("Name2"))
                    .verifyComplete();
    }
}
