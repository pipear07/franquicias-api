package com.franquicias.domain.repository;

import com.franquicias.domain.model.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {
  
  Mono<Franchise> save(Franchise franchise);

  Mono<Franchise> findById(String id);

  Flux<Franchise> findAll();

  Mono<Void> deleteById(String id);
}