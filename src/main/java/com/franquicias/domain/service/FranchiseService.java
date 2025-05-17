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
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Service with basic operations; we will expand later
 */
@Service
@RequiredArgsConstructor
public class FranchiseService {

    private static final Logger log = LoggerFactory.getLogger(FranchiseService.class);

    private final FranchiseReactiveRepository repo;
    private final ModelMapper mapper;

    public Mono<Franchise> save(Franchise dto) {
        log.debug("Saving franchise: {}", dto.getName());
        return repo.save(mapper.map(dto, FranchiseEntity.class))
                   .map(ent -> {
                       log.debug("Franchise saved with id={}", ent.getId());
                       return mapper.map(ent, Franchise.class);
                   });
    }

    public Flux<Franchise> findAll() {
        log.debug("Retrieving all franchises");
        return repo.findAll()
                   .map(ent -> mapper.map(ent, Franchise.class))
                   .doOnComplete(() -> log.debug("Completed retrieval of all franchises"));
    }

    public Mono<Franchise> findById(String id) {
        log.debug("Finding franchise by id={}", id);
        return repo.findById(id)
                   .map(ent -> mapper.map(ent, Franchise.class))
                   .doOnError(e -> log.warn("Error finding franchise {}: {}", id, e.getMessage()));
    }

    public Mono<Franchise> addBranch(String franchiseId, Branch branch) {
        log.debug("Adding branch {} to franchise {}", branch.getName(), franchiseId);
        return repo.findById(franchiseId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> { // I mutate the entity
                if (ent.getBranches() == null) {
                    ent.setBranches(new ArrayList<>());
                }
                ent.getBranches().add(mapper.map(branch, BranchEntity.class));
                log.debug("Branch {} added to franchise {}", branch.getName(), franchiseId);
                return ent;
            })
            .flatMap(repo::save)
            .map(e -> mapper.map(e, Franchise.class));
    }

    public Mono<Franchise> addProduct(String franchiseId, String branchId, Product product) {
        log.debug("Adding product {} to branch {} of franchise {}", product.getName(), branchId, franchiseId);
        return repo.findById(franchiseId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> {
                BranchEntity branch = ent.getBranches().stream() // find a branch
                    .filter(b -> b.getId().equals(branchId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
                if (branch.getProducts() == null) { // initialize product list if null is present
                    branch.setProducts(new ArrayList<>());
                }
                branch.getProducts().add(mapper.map(product, ProductEntity.class)); // add mapped product
                log.debug("Product {} added to branch {}", product.getName(), branchId);
                return ent;
            })
            .flatMap(repo::save) // save to Mongo
            .map(saved -> mapper.map(saved, Franchise.class));
    }

    public Mono<Franchise> updateStock(String fid, String bid, String pid, int newStock) {
        log.debug("Updating stock for product {} in branch {} of franchise {} to {}", pid, bid, fid, newStock);
        return repo.findById(fid)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> {
                // find the branch
                BranchEntity branch = ent.getBranches().stream()
                    .filter(b -> b.getId().equals(bid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
                // search for the product
                    ProductEntity prod = branch.getProducts().stream()
                    .filter(p -> p.getId().equals(pid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
                prod.setStock(newStock);
                log.debug("Stock for product {} updated to {}", pid, newStock);
                return ent;
            })
            .flatMap(repo::save)
            .map(saved -> mapper.map(saved, Franchise.class));
    }

    public Mono<Void> deleteProduct(String fid, String bid, String pid) {
        log.debug("Deleting product {} from branch {} of franchise {}", pid, bid, fid);
        return repo.findById(fid)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .flatMap(ent -> {
                BranchEntity branch = ent.getBranches().stream()
                    .filter(b -> b.getId().equals(bid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
                boolean removed = branch.getProducts().removeIf(p -> p.getId().equals(pid));
                if (!removed) {
                    log.warn("Product {} not found in branch {}", pid, bid);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                }
                log.debug("Product {} removed from branch {}", pid, bid);
                return repo.save(ent).then();
            });
    }

    public Flux<ProductStockDto> topStockByBranch(String fid) {
        log.debug("Getting top stock by branch for franchise {}", fid);
        return repo.findById(fid)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .flatMapMany(ent -> Flux.fromIterable(ent.getBranches()))
            .filter(br -> br.getProducts() != null && !br.getProducts().isEmpty())
            .map(br -> {
                ProductEntity top = br.getProducts().stream()
                    .max(Comparator.comparingInt(ProductEntity::getStock))
                    .orElseThrow();
                return new ProductStockDto(
                    top.getId(), top.getName(), br.getId(), br.getName(), top.getStock()
                );
            });
    }

    // Rename franchise
    public Mono<Franchise> renameFranchise(String fid, String newName) {
        log.debug("Renaming franchise {} to {}", fid, newName);
        return repo.findById(fid)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> {
                ent.setName(newName);
                return ent;
            })
            .flatMap(repo::save)
            .map(e -> mapper.map(e, Franchise.class));
    }

    // Rename branch
    public Mono<Franchise> renameBranch(String fid, String bid, String newName) {
        log.debug("Renaming branch {} in franchise {} to {}", bid, fid, newName);
        return repo.findById(fid)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> {
                BranchEntity br = ent.getBranches().stream()
                    .filter(b -> b.getId().equals(bid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
                br.setName(newName);
                return ent;
            })
            .flatMap(repo::save)
            .map(e -> mapper.map(e, Franchise.class));
    }
    
    // Rename product
    public Mono<Franchise> renameProduct(String fid, String bid, String pid, String newName) {
        log.debug("Renaming product {} in branch {} of franchise {} to {}", pid, bid, fid, newName);
        return repo.findById(fid)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")))
            .map(ent -> {
                BranchEntity br = ent.getBranches().stream()
                    .filter(b -> b.getId().equals(bid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
                ProductEntity pr = br.getProducts().stream()
                    .filter(p -> p.getId().equals(pid))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
                pr.setName(newName);
                return ent;
            })
            .flatMap(repo::save)
            .map(e -> mapper.map(e, Franchise.class));
    }
}
