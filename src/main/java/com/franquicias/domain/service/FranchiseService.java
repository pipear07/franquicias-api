package com.franquicias.domain.service;

import com.franquicias.api.dto.ProductStockDto;
import com.franquicias.domain.model.Branch;
import com.franquicias.domain.model.Franchise;
import com.franquicias.domain.model.Product;
import com.franquicias.infrastructure.db.BranchEntity;
import com.franquicias.infrastructure.db.FranchiseEntity;
import com.franquicias.infrastructure.db.FranchiseReactiveRepository;
import com.franquicias.infrastructure.db.ProductEntity;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.Comparator;


/**
 * Service with basic operations; we will expand later
 */
@Service
@RequiredArgsConstructor
public class FranchiseService {

    private final FranchiseReactiveRepository repo;
    private final ModelMapper mapper;

    public Mono<Franchise> save(Franchise dto) {
        return repo.save(mapper.map(dto, FranchiseEntity.class))
                   .map(ent -> mapper.map(ent, Franchise.class));
    }

    public Flux<Franchise> findAll() {
        return repo.findAll()
                   .map(ent -> mapper.map(ent, Franchise.class));
    }

    public Mono<Franchise> findById(String id) {
        return repo.findById(id)
                   .map(ent -> mapper.map(ent, Franchise.class));
    }

    public Mono<Franchise> addBranch(String franchiseId, Branch branch) {
        return repo.findById(franchiseId)
                .switchIfEmpty(Mono.error( new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
                .map(ent -> { // I mutate the entity
                        if (ent.getBranches() == null) ent.setBranches(new ArrayList<>());
                        ent.getBranches().add(mapper.map(branch, BranchEntity.class));
                        return ent;
                })
                .flatMap(repo::save)
                .map(e -> mapper.map(e, Franchise.class));
    }

    public Mono<Franchise> addProduct(String franchiseId,
                                  String branchId,
                                  Product product) {

        return repo.findById(franchiseId)
            .switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> {
                // 1. buscar sucursal
                BranchEntity branch = ent.getBranches().stream()
                    .filter(b -> b.getId().equals(branchId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Branch not found"));

                // 2. inicializar lista de productos si viene null
                if (branch.getProducts() == null) branch.setProducts(new ArrayList<>());

                // 3. agregar producto mapeado
                branch.getProducts().add(mapper.map(product, ProductEntity.class));
                return ent;
            })
            .flatMap(repo::save)                     // guardar en Mongo
            .map(saved -> mapper.map(saved, Franchise.class));
    }

    public Mono<Franchise> updateStock(String fid,
                                   String bid,
                                   String pid,
                                   int newStock) {

        return repo.findById(fid)
            .switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> {
                // ── buscar la sucursal ──
                BranchEntity branch = ent.getBranches().stream()
                    .filter(b -> b.getId().equals(bid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Branch not found"));

                // ── buscar el producto ──
                ProductEntity prod = branch.getProducts().stream()
                    .filter(p -> p.getId().equals(pid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

                // ── actualizar stock ──
                prod.setStock(newStock);
                return ent;                 // devolvemos la entidad modificada
            })
            .flatMap(repo::save)            // persistimos en Mongo
            .map(saved -> mapper.map(saved, Franchise.class));
    }


    public Mono<Void> deleteProduct(String fid,
                                String bid,
                                String pid) {

        return repo.findById(fid)
            .switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .flatMap(ent -> {
                // ── buscar la sucursal ──
                BranchEntity branch = ent.getBranches().stream()
                    .filter(b -> b.getId().equals(bid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Branch not found"));

                // ── eliminar el producto ──
                boolean removed = branch.getProducts().removeIf(p -> p.getId().equals(pid));
                if (!removed) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found");
                }
                return repo.save(ent).then();   // .then() → Mono<Void>
            });
    }


    public Flux<ProductStockDto> topStockByBranch(String fid) {

        return repo.findById(fid)
            .switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .flatMapMany(ent -> Flux.fromIterable(ent.getBranches()))
            .filter(br -> br.getProducts() != null && !br.getProducts().isEmpty())
            .map(br -> {
                // Obtener el producto con más stock dentro de la sucursal
                ProductEntity top = br.getProducts().stream()
                    .max(Comparator.comparingInt(ProductEntity::getStock))
                    .orElseThrow();   // nunca null por el filter anterior

                return new ProductStockDto(
                        top.getId(),
                        top.getName(),
                        br.getId(),
                        br.getName(),
                        top.getStock());
            });
    }




}
