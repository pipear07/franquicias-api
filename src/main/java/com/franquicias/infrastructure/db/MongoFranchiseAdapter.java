package com.franquicias.infrastructure.db;

import com.franquicias.domain.model.Franchise;
import com.franquicias.domain.repository.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MongoFranchiseAdapter implements FranchiseRepository {

    private final ReactiveMongoTemplate template;
    private final ModelMapper mapper;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        // Convertimos el dominio a entidad
        var entity = mapper.map(franchise, FranchiseEntity.class);
        return template.save(entity)
                       // Convertimos la entidad guardada de vuelta a dominio
                       .map(e -> mapper.map(e, Franchise.class));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return template.findById(id, FranchiseEntity.class)
                       .map(e -> mapper.map(e, Franchise.class));
    }

    @Override
    public Flux<Franchise> findAll() {
        return template.findAll(FranchiseEntity.class)
                       .map(e -> mapper.map(e, Franchise.class));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return template.remove(
                   Query.query(Criteria.where("_id").is(id)),
                   FranchiseEntity.class
               )
               .then();
    }
}
