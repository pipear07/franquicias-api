package com.franquicias.api;

import com.franquicias.api.dto.AddBranchRequest;
import com.franquicias.api.dto.AddProductRequest;
import com.franquicias.api.dto.ProductStockDto;
import com.franquicias.api.dto.RenameRequest;
import com.franquicias.api.dto.UpdateStockRequest;
import com.franquicias.domain.model.Branch;
import com.franquicias.domain.model.Franchise;
import com.franquicias.domain.model.Product;
import com.franquicias.domain.service.FranchiseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper; // It is a small library that is responsible for copying data from one object to another automatically
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/franchises")
@RequiredArgsConstructor
public class FranchiseController {

    private static final Logger log = LoggerFactory.getLogger(FranchiseController.class);

    private final FranchiseService service;
    private final ModelMapper mapper;

    @GetMapping
    public Flux<Franchise> list() {
        log.info("Received GET /franchises");
        return service.findAll()
                      .doOnNext(f -> log.debug("Returning franchise {}", f.getId()));
    }

    @GetMapping("/{id}")
    public Mono<Franchise> get(@PathVariable String id) {
        log.info("Received GET /franchises/{}", id);
        return service.findById(id)
                      .switchIfEmpty(Mono.error(
                          new ResponseStatusException(HttpStatus.NOT_FOUND, "Franchise not found")
                      ))
                      .doOnError(ex -> log.warn("Franchise {} not found", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franchise> create(@Valid @RequestBody Franchise dto) {
        log.info("Received POST /franchises with body {}", dto);
        return service.save(dto)
                      .doOnSuccess(f -> log.info("Created franchise {}", f.getId()));
    }

    @PostMapping("/{id}/branches")
    public Mono<Franchise> addBranch(@PathVariable String id,
                                     @Valid @RequestBody AddBranchRequest req) {
        log.info("Received POST /franchises/{}/branches with body {}", id, req);
        Branch branch = mapper.map(req, Branch.class);
        return service.addBranch(id, branch)
                      .doOnSuccess(f -> log.info("Added branch {} to franchise {}", branch.getId(), id));
    }

    @PostMapping("/{fid}/branches/{bid}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franchise> addProduct(@PathVariable String fid,
                                       @PathVariable String bid,
                                       @Valid @RequestBody AddProductRequest body) {
        log.info("Received POST /franchises/{}/branches/{}/products with body {}", fid, bid, body);
        Product product = mapper.map(body, Product.class);
        return service.addProduct(fid, bid, product)
                      .doOnSuccess(f -> log.info("Added product {} to branch {} of franchise {}", product.getId(), bid, fid));
    }

    @PatchMapping("/{fid}/branches/{bid}/products/{pid}/stock")
    public Mono<Franchise> updateStock(@PathVariable String fid,
                                       @PathVariable String bid,
                                       @PathVariable String pid,
                                       @Valid @RequestBody UpdateStockRequest body) {
        log.info("Received PATCH /franchises/{}/branches/{}/products/{}/stock with body {}", fid, bid, pid, body);
        return service.updateStock(fid, bid, pid, body.getStock())
                      .doOnSuccess(f -> log.info("Updated stock for product {} to {}", pid, body.getStock()));
    }

    @DeleteMapping("/{fid}/branches/{bid}/products/{pid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProduct(@PathVariable String fid,
                                    @PathVariable String bid,
                                    @PathVariable String pid) {
        log.info("Received DELETE /franchises/{}/branches/{}/products/{}", fid, bid, pid);
        return service.deleteProduct(fid, bid, pid)
                      .doOnSuccess(v -> log.info("Deleted product {} from branch {} of franchise {}", pid, bid, fid));
    }

    @GetMapping("/{fid}/top-stock")
    public Flux<ProductStockDto> topStockByBranch(@PathVariable String fid) {
        log.info("Received GET /franchises/{}/top-stock", fid);
        return service.topStockByBranch(fid)
                      .doOnNext(dto -> log.debug("Top stock for branch {}: {}", dto.getBranchId(), dto.getProductId()));
    }

    // Rename franchise
    @PatchMapping("/{fid}")
    public Mono<Franchise> renameFranchise(
        @PathVariable String fid,
        @Valid @RequestBody RenameRequest body) {
        log.info("Received PATCH /franchises/{} with rename body {}", fid, body);
        return service.renameFranchise(fid, body.getName())
                      .doOnSuccess(f -> log.info("Renamed franchise {} to {}", fid, body.getName()));
    }

    // Rename branch
    @PatchMapping("/{fid}/branches/{bid}")
    public Mono<Franchise> renameBranch(
        @PathVariable String fid,
        @PathVariable String bid,
        @Valid @RequestBody RenameRequest body) {
        log.info("Received PATCH /franchises/{}/branches/{} with rename body {}", fid, bid, body);
        return service.renameBranch(fid, bid, body.getName())
                      .doOnSuccess(f -> log.info("Renamed branch {} in franchise {} to {}", bid, fid, body.getName()));
    }

    // Rename product
    @PatchMapping("/{fid}/branches/{bid}/products/{pid}")
    public Mono<Franchise> renameProduct(
        @PathVariable String fid,
        @PathVariable String bid,
        @PathVariable String pid,
        @Valid @RequestBody RenameRequest body) {
        log.info("Received PATCH /franchises/{}/branches/{}/products/{} with rename body {}", fid, bid, pid, body);
        return service.renameProduct(fid, bid, pid, body.getName())
                      .doOnSuccess(f -> log.info("Renamed product {} in branch {} of franchise {} to {}", pid, bid, fid, body.getName()));
    }
}
