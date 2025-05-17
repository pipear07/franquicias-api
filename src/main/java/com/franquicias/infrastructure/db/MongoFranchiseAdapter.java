package com.franquicias.infrastructure.db;

import com.franquicias.domain.model.Franchise;
import com.franquicias.domain.repository.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter for persistence in reactive MongoDB
 */
@Component
@RequiredArgsConstructor
public class MongoFranchiseAdapter implements FranchiseRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoFranchiseAdapter.class);

    private final ReactiveMongoTemplate template;
    private final ModelMapper mapper;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        log.debug("Saving franchise to Mongo: {}", franchise.getId());
        var entity = mapper.map(franchise, FranchiseEntity.class);
        return template.save(entity)
                       .doOnSuccess(e -> log.info("Franchise saved to Mongo: {}", e.getId()))
                       .doOnError(err -> log.error("Error saving franchise {}: {}", franchise.getId(), err.getMessage()))
                       .map(e -> mapper.map(e, Franchise.class));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        log.debug("Finding franchise by id in Mongo: {}", id);
        return template.findById(id, FranchiseEntity.class)
                       .doOnNext(e -> log.info("Franchise found in Mongo: {}", e.getId()))
                       .doOnError(err -> log.error("Error finding franchise {}: {}", id, err.getMessage()))
                       .map(e -> mapper.map(e, Franchise.class));
    }

    @Override
    public Flux<Franchise> findAll() {
        log.debug("Retrieving all franchises from Mongo");
        return template.findAll(FranchiseEntity.class)
                       .doOnNext(e -> log.debug("Found franchise in Mongo: {}", e.getId()))
                       .map(e -> mapper.map(e, Franchise.class))
                       .doOnComplete(() -> log.info("Completed retrieval of all franchises from Mongo"));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        log.debug("Deleting franchise in Mongo by id: {}", id);
        return template.remove(
                   Query.query(Criteria.where("_id").is(id)),
                   FranchiseEntity.class)
               .doOnSuccess(res -> log.info("Deleted franchise {} from Mongo, deletedCount={}", id, res.getDeletedCount()))
               .doOnError(err -> log.error("Error deleting franchise {}: {}", id, err.getMessage()))
               .then();
    }
}
