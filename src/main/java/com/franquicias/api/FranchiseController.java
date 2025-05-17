package com.franquicias.api;

import com.franquicias.api.dto.AddBranchRequest;
import com.franquicias.api.dto.AddProductRequest;
import com.franquicias.api.dto.ProductStockDto;
import com.franquicias.api.dto.UpdateStockRequest;
import com.franquicias.domain.model.Branch;
import com.franquicias.domain.model.Franchise;
import com.franquicias.domain.model.Product;
import com.franquicias.domain.service.FranchiseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.modelmapper.ModelMapper;      // ← IMPORTA ESTO

@RestController
@RequestMapping("/franchises")
@RequiredArgsConstructor
public class FranchiseController {

    private final FranchiseService service;
    private final ModelMapper mapper;

    @GetMapping
    public Flux<Franchise> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Franchise> get(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franchise> create(@Valid @RequestBody Franchise dto) {
        return service.save(dto);
    }

    @PostMapping("/{id}/branches")
    public Mono<Franchise> addBranch(@PathVariable String id,
                                    @Valid @RequestBody AddBranchRequest req) {
        Branch branch = mapper.map(req, Branch.class);
        return service.addBranch(id, branch);
    }

    @PostMapping("/{fid}/branches/{bid}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franchise> addProduct(@PathVariable String fid,
                                    @PathVariable String bid,
                                    @Valid @RequestBody AddProductRequest body) {

        Product product = mapper.map(body, Product.class);
        return service.addProduct(fid, bid, product);
    }

    @PatchMapping("/{fid}/branches/{bid}/products/{pid}/stock")
    public Mono<Franchise> updateStock(@PathVariable String fid,
                                    @PathVariable String bid,
                                    @PathVariable String pid,
                                    @Valid @RequestBody UpdateStockRequest body) {

        return service.updateStock(fid, bid, pid, body.getStock());
    }

    @DeleteMapping("/{fid}/branches/{bid}/products/{pid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)      
    public Mono<Void> deleteProduct(@PathVariable String fid,
                                    @PathVariable String bid,
                                    @PathVariable String pid) {
        return service.deleteProduct(fid, bid, pid);
    }


    @GetMapping("/{fid}/top-stock")
    public Flux<ProductStockDto> topStockByBranch(@PathVariable String fid) {
        return service.topStockByBranch(fid);
    }


}
