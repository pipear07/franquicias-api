package com.franquicias.infrastructure.db;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FranchiseReactiveRepository
        extends ReactiveMongoRepository<FranchiseEntity, String> {
}
