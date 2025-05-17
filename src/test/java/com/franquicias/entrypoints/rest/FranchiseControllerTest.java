package com.franquicias.entrypoints.rest;

import com.franquicias.api.FranchiseController;
import com.franquicias.api.dto.AddBranchRequest;
import com.franquicias.api.dto.AddProductRequest;
import com.franquicias.api.dto.ProductStockDto;
import com.franquicias.api.dto.RenameRequest;
import com.franquicias.api.dto.UpdateStockRequest;
import com.franquicias.domain.model.Branch;
import com.franquicias.domain.model.Franchise;
import com.franquicias.domain.model.Product;
import com.franquicias.domain.service.FranchiseService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebFluxTest(controllers = FranchiseController.class)
class FranchiseControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private FranchiseService service;

    @MockitoBean
    private ModelMapper mapper;

    @Test
    void listAll_returnsFluxOfFranchise() {
        Franchise f1 = new Franchise("F1","Fr1", List.of());
        given(service.findAll()).willReturn(Flux.just(f1));

        client.get().uri("/franchises")
              .accept(MediaType.APPLICATION_JSON)
              .exchange()
              .expectStatus().isOk()
              .expectBodyList(Franchise.class)
              .hasSize(1)
              .contains(f1);
    }

    @Test
    void getById_found_returns200() {
        Franchise f = new Franchise("F1","Fr1", List.of());
        given(service.findById("F1")).willReturn(Mono.just(f));

        client.get().uri("/franchises/F1")
              .exchange()
              .expectStatus().isOk()
              .expectBody(Franchise.class)
              .isEqualTo(f);
    }

    @Test
    void create_returns201AndBody() {
        Franchise in = new Franchise("F1","Fr1", null);
        Franchise saved = new Franchise("F1","Fr1", List.of());
        given(service.save(in)).willReturn(Mono.just(saved));

        client.post().uri("/franchises")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(in)
              .exchange()
              .expectStatus().isCreated()
              .expectBody(Franchise.class)
              .isEqualTo(saved);
    }

    @Test
    void addBranch_mapsAndReturns200() {
        AddBranchRequest req = new AddBranchRequest("B1","S1");
        Branch branch = new Branch("B1","S1", List.of());
        Franchise updated = new Franchise("F1","Fr1", List.of(branch));

        given(mapper.map(req, Branch.class)).willReturn(branch);
        given(service.addBranch(eq("F1"), eq(branch)))
            .willReturn(Mono.just(updated));

        client.post().uri("/franchises/F1/branches")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isOk()
              .expectBody(Franchise.class)
              .isEqualTo(updated);
    }

    @Test
    void addProduct_mapsAndReturns201() {
        AddProductRequest req = new AddProductRequest("P1","Prod",5);
        Product prod = new Product("P1","Prod",5);
        Franchise updated = new Franchise("F1","Fr1",
            List.of(new Branch("B1","S1", List.of(prod)))
        );

        given(mapper.map(req, Product.class)).willReturn(prod);
        given(service.addProduct(eq("F1"), eq("B1"), eq(prod)))
            .willReturn(Mono.just(updated));

        client.post().uri("/franchises/F1/branches/B1/products")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isCreated()
              .expectBody(Franchise.class)
              .isEqualTo(updated);
    }

    @Test
    void updateStock_returns200() {
        UpdateStockRequest req = new UpdateStockRequest(10);
        Franchise updated = new Franchise("F1","Fr1",
            List.of(new Branch("B1","S1", List.of(new Product("P1","X",10))))
        );

        given(service.updateStock(eq("F1"), eq("B1"), eq("P1"), eq(10)))
            .willReturn(Mono.just(updated));

        client.patch().uri("/franchises/F1/branches/B1/products/P1/stock")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isOk()
              .expectBody(Franchise.class)
              .isEqualTo(updated);
    }

    @Test
    void deleteProduct_returns204() {
        given(service.deleteProduct(eq("F1"), eq("B1"), eq("P1")))
            .willReturn(Mono.empty());

        client.delete().uri("/franchises/F1/branches/B1/products/P1")
              .exchange()
              .expectStatus().isNoContent();
    }

    @Test
    void topStockByBranch_returns200() {
        ProductStockDto dto1 = new ProductStockDto("P1","X","B1","S1",5);
        given(service.topStockByBranch("F1")).willReturn(Flux.just(dto1));

        client.get().uri("/franchises/F1/top-stock")
              .exchange()
              .expectStatus().isOk()
              .expectBodyList(ProductStockDto.class)
              .hasSize(1)
              .contains(dto1);
    }

    @Test
    void renameFranchise_returns200() {
        RenameRequest req = new RenameRequest("NewName");
        Franchise renamed = new Franchise("F1","NewName", List.of());

        given(service.renameFranchise(eq("F1"), eq("NewName")))
            .willReturn(Mono.just(renamed));

        client.patch().uri("/franchises/F1")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isOk()
              .expectBody(Franchise.class)
              .isEqualTo(renamed);
    }

    @Test
    void renameBranch_returns200() {
        RenameRequest req = new RenameRequest("NewB");
        Franchise renamed = new Franchise("F1","Fr1",
            List.of(new Branch("B1","NewB", List.of()))
        );

        given(service.renameBranch(eq("F1"), eq("B1"), eq("NewB")))
            .willReturn(Mono.just(renamed));

        client.patch().uri("/franchises/F1/branches/B1")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isOk()
              .expectBody(Franchise.class)
              .isEqualTo(renamed);
    }

    @Test
    void renameProduct_returns200() {
        RenameRequest req = new RenameRequest("NewP");
        Franchise renamed = new Franchise("F1","Fr1",
            List.of(new Branch("B1","S1",
                List.of(new Product("P1","NewP",3))))
        );

        given(service.renameProduct(eq("F1"), eq("B1"), eq("P1"), eq("NewP")))
            .willReturn(Mono.just(renamed));

        client.patch().uri("/franchises/F1/branches/B1/products/P1")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isOk()
              .expectBody(Franchise.class)
              .isEqualTo(renamed);
    }

    // 
    // Error paths (404 only)
    // 

    @Test
    void getById_notFound_returns404() {
        given(service.findById("X")).willReturn(Mono.empty());

        client.get().uri("/franchises/X")
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void addBranch_notFound_returns404() {
        AddBranchRequest req = new AddBranchRequest("B1","S1");
        given(mapper.map(req, Branch.class)).willReturn(new Branch("B1","S1", List.of()));
        given(service.addBranch(eq("NOPE"), any(Branch.class)))
            .willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        client.post().uri("/franchises/NOPE/branches")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void addProduct_notFound_returns404() {
        AddProductRequest req = new AddProductRequest("P1","Prod",5);
        given(mapper.map(req, Product.class)).willReturn(new Product("P1","Prod",5));
        given(service.addProduct(eq("F1"), eq("B1"), any(Product.class)))
            .willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        client.post().uri("/franchises/F1/branches/B1/products")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void updateStock_notFound_returns404() {
        UpdateStockRequest req = new UpdateStockRequest(10);
        given(service.updateStock(eq("F1"), eq("B1"), eq("P1"), eq(10)))
            .willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        client.patch().uri("/franchises/F1/branches/B1/products/P1/stock")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void deleteProduct_notFound_returns404() {
        given(service.deleteProduct(eq("F1"), eq("B1"), eq("P1")))
            .willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        client.delete().uri("/franchises/F1/branches/B1/products/P1")
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void renameBranch_notFound_returns404() {
        RenameRequest req = new RenameRequest("X");
        given(service.renameBranch(eq("F1"), eq("B1"), eq("X")))
            .willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        client.patch().uri("/franchises/F1/branches/B1")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void renameProduct_notFound_returns404() {
        RenameRequest req = new RenameRequest("X");
        given(service.renameProduct(eq("F1"), eq("B1"), eq("P1"), eq("X")))
            .willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        client.patch().uri("/franchises/F1/branches/B1/products/P1")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(req)
              .exchange()
              .expectStatus().isNotFound();
    }
}
