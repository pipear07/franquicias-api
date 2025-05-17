package com.franquicias.domain.service;

import com.franquicias.api.dto.ProductStockDto;
import com.franquicias.domain.model.Branch;
import com.franquicias.domain.model.Franchise;
import com.franquicias.domain.model.Product;
import com.franquicias.infrastructure.db.BranchEntity;
import com.franquicias.infrastructure.db.FranchiseEntity;
import com.franquicias.infrastructure.db.FranchiseReactiveRepository;
import com.franquicias.infrastructure.db.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FranchiseServiceTest {

    @Mock FranchiseReactiveRepository repo;
    @Spy  ModelMapper mapper = new ModelMapper();
    @InjectMocks FranchiseService service;

    FranchiseEntity baseEntity;
    Franchise       baseDto;

    @BeforeEach
    void setUp() {
        baseEntity = new FranchiseEntity("F1","Fr1", new ArrayList<>());
        baseDto    = new Franchise("F1","Fr1", new ArrayList<>());
    }

    @Test
    void save_success() {
        when(repo.save(any())).thenReturn(Mono.just(baseEntity));
        doReturn(baseDto).when(mapper).map(baseEntity, Franchise.class);

        StepVerifier.create(service.save(new Franchise("F1","Fr1",null)))
                    .expectNext(baseDto)
                    .verifyComplete();
    }

    @Test
    void findAll_nonEmpty() {
        when(repo.findAll()).thenReturn(Flux.just(baseEntity));
        doReturn(baseDto).when(mapper).map(baseEntity, Franchise.class);

        StepVerifier.create(service.findAll())
                    .expectNext(baseDto)
                    .verifyComplete();
    }

    @Test
    void findById_notFound() {
        when(repo.findById("X")).thenReturn(Mono.empty());
        StepVerifier.create(service.findById("X")).verifyComplete();
    }

    @Test
    void findById_success() {
        when(repo.findById("F1")).thenReturn(Mono.just(baseEntity));
        doReturn(baseDto).when(mapper).map(baseEntity, Franchise.class);

        StepVerifier.create(service.findById("F1"))
                    .expectNext(baseDto)
                    .verifyComplete();
    }

    @Test
    void renameFranchise_notFound() {
        when(repo.findById("F1")).thenReturn(Mono.empty());
        StepVerifier.create(service.renameFranchise("F1","X"))
                    .expectErrorMatches(ex ->
                        ex instanceof ResponseStatusException &&
                        ((ResponseStatusException)ex).getStatusCode().value() == 404
                    ).verify();
    }

    @Test
    void renameFranchise_success() {
        FranchiseEntity orig = new FranchiseEntity("F1","Old",new ArrayList<>());
        FranchiseEntity saved= new FranchiseEntity("F1","New",new ArrayList<>());
        Franchise dto       = new Franchise("F1","New",new ArrayList<>());

        when(repo.findById("F1")).thenReturn(Mono.just(orig));
        when(repo.save(argThat(e->e.getName().equals("New"))))
            .thenReturn(Mono.just(saved));
        doReturn(dto).when(mapper).map(saved,Franchise.class);

        StepVerifier.create(service.renameFranchise("F1","New"))
                    .expectNext(dto)
                    .verifyComplete();
    }

    @Test
    void addBranch_success() {
        BranchEntity be = new BranchEntity("B1","S1",new ArrayList<>());
        FranchiseEntity upd = new FranchiseEntity("F1","Fr1", List.of(be));
        Franchise dto = new Franchise("F1","Fr1", List.of(new Branch("B1","S1",new ArrayList<>())));

        when(repo.findById("F1")).thenReturn(Mono.just(baseEntity));
        when(repo.save(argThat(e->e.getBranches().size()==1)))
            .thenReturn(Mono.just(upd));
        doReturn(dto).when(mapper).map(upd,Franchise.class);

        StepVerifier.create(service.addBranch("F1", new Branch("B1","S1",new ArrayList<>())))
                    .expectNext(dto)
                    .verifyComplete();
    }

    @Test
    void addProduct_success() {
        BranchEntity b = new BranchEntity("B1","S1",new ArrayList<>());
        baseEntity.setBranches(List.of(b));

        ProductEntity pe = new ProductEntity("P1","Prod",5);
        BranchEntity b2 = new BranchEntity("B1","S1", List.of(pe));
        FranchiseEntity upd = new FranchiseEntity("F1","Fr1", List.of(b2));
        Franchise dto = new Franchise("F1","Fr1",
                             List.of(new Branch("B1","S1", List.of(new Product("P1","Prod",5)))));

        when(repo.findById("F1")).thenReturn(Mono.just(baseEntity));
        when(repo.save(argThat(e-> e.getBranches().get(0).getProducts().size()==1)))
            .thenReturn(Mono.just(upd));
        doReturn(dto).when(mapper).map(upd,Franchise.class);

        StepVerifier.create(service.addProduct("F1","B1", new Product("P1","Prod",5)))
                    .expectNext(dto)
                    .verifyComplete();
    }

    @Test
    void updateStock_success() {
        ProductEntity p = new ProductEntity("P1","X",1);
        BranchEntity b = new BranchEntity("B1","S1", List.of(p));
        baseEntity.setBranches(List.of(b));

        BranchEntity b2 = new BranchEntity("B1","S1", List.of(new ProductEntity("P1","X", 10)));
        FranchiseEntity upd = new FranchiseEntity("F1","Fr1", List.of(b2));
        Franchise dto = new Franchise("F1","Fr1", List.of(new Branch("B1","S1", List.of(new Product("P1","X",10)))));

        when(repo.findById("F1")).thenReturn(Mono.just(baseEntity));
        when(repo.save(any())).thenReturn(Mono.just(upd));
        doReturn(dto).when(mapper).map(upd,Franchise.class);

        StepVerifier.create(service.updateStock("F1","B1","P1",10))
                    .expectNext(dto)
                    .verifyComplete();
    }

    @Test
    void deleteProduct_success() {
        ProductEntity p = new ProductEntity("P1", "X", 1);
        BranchEntity b = new BranchEntity("B1", "S1", new ArrayList<>(List.of(p)));
        
        baseEntity.setBranches(new ArrayList<>(List.of(b)));


        when(repo.findById("F1")).thenReturn(Mono.just(baseEntity));


        FranchiseEntity updated = new FranchiseEntity(
            "F1", 
            "Fr1", 
            new ArrayList<>(List.of(
                new BranchEntity("B1", "S1", new ArrayList<>())
            ))
        );
        when(repo.save(any())).thenReturn(Mono.just(updated));


        StepVerifier.create(service.deleteProduct("F1", "B1", "P1"))
                    .verifyComplete();
    }


    @Test
    void topStockByBranch_success() {
        ProductEntity p1 = new ProductEntity("P1","A",3);
        ProductEntity p2 = new ProductEntity("P2","B",5);
        BranchEntity b1 = new BranchEntity("B1","S1", List.of(p1,p2));
        ProductEntity q1 = new ProductEntity("Q1","C",7);
        BranchEntity b2 = new BranchEntity("B2","S2", List.of(q1));
        baseEntity.setBranches(List.of(b1,b2));

        when(repo.findById("F1")).thenReturn(Mono.just(baseEntity));

        StepVerifier.create(service.topStockByBranch("F1"))
                    .expectNext(new ProductStockDto("P2","B","B1","S1",5))
                    .expectNext(new ProductStockDto("Q1","C","B2","S2",7))
                    .verifyComplete();
    }


    @Test void addBranch_notFound_404() {
        given(repo.findById("F1")).willReturn(Mono.empty());
        StepVerifier.create(service.addBranch("F1", new Branch("B1","S1",List.of())))
                    .expectErrorMatches(ex->
                      ex instanceof ResponseStatusException &&
                      ((ResponseStatusException)ex).getStatusCode().value()==404
                    ).verify();
    }

    @Test void addProduct_franchiseNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.empty());
        StepVerifier.create(service.addProduct("F1","B1", new Product("P1","X",1)))
                    .expectError(ResponseStatusException.class)
                    .verify();
    }
    @Test void addProduct_branchNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.addProduct("F1","B1", new Product("P1","X",1)))
                    .expectErrorMatches(ex->
                      ex instanceof ResponseStatusException &&
                      ((ResponseStatusException)ex).getStatusCode().value()==404
                    ).verify();
    }

    @Test void updateStock_franchiseNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.empty());
        StepVerifier.create(service.updateStock("F1","B1","P1",10))
                    .expectError(ResponseStatusException.class).verify();
    }
    @Test void updateStock_branchNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.updateStock("F1","B1","P1",10))
                    .expectError(ResponseStatusException.class).verify();
    }
    @Test void updateStock_productNotFound_404() {
        baseEntity.setBranches(List.of(new BranchEntity("B1","S1",new ArrayList<>())));
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.updateStock("F1","B1","P1",10))
                    .expectError(ResponseStatusException.class).verify();
    }

    @Test void deleteProduct_franchiseNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.empty());
        StepVerifier.create(service.deleteProduct("F1","B1","P1"))
                    .expectError(ResponseStatusException.class).verify();
    }
    @Test void deleteProduct_branchNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.deleteProduct("F1","B1","P1"))
                    .expectError(ResponseStatusException.class).verify();
    }
    @Test void deleteProduct_productNotFound_404() {
        baseEntity.setBranches(List.of(new BranchEntity("B1","S1",new ArrayList<>())));
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.deleteProduct("F1","B1","P1"))
                    .expectError(ResponseStatusException.class).verify();
    }

    @Test void topStockByBranch_notFound_404() {
        given(repo.findById("F1")).willReturn(Mono.empty());
        StepVerifier.create(service.topStockByBranch("F1"))
                    .expectError(ResponseStatusException.class).verify();
    }

    @Test void renameBranch_franchiseNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.empty());
        StepVerifier.create(service.renameBranch("F1","B1","X"))
                    .expectError(ResponseStatusException.class).verify();
    }
    @Test void renameBranch_branchNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.renameBranch("F1","B1","X"))
                    .expectError(ResponseStatusException.class).verify();
    }

    @Test void renameProduct_franchiseNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.empty());
        StepVerifier.create(service.renameProduct("F1","B1","P1","X"))
                    .expectError(ResponseStatusException.class).verify();
    }
    @Test void renameProduct_branchNotFound_404() {
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.renameProduct("F1","B1","P1","X"))
                    .expectError(ResponseStatusException.class).verify();
    }
    @Test void renameProduct_productNotFound_404() {
        baseEntity.setBranches(List.of(new BranchEntity("B1","S1",new ArrayList<>())));
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        StepVerifier.create(service.renameProduct("F1","B1","P1","X"))
                    .expectError(ResponseStatusException.class).verify();
    }

    @Test
    void renameBranch_success_path() {
        BranchEntity b = new BranchEntity("B1","Old",new ArrayList<>());
        baseEntity.setBranches(List.of(b));
        FranchiseEntity upd = new FranchiseEntity("F1","Fr1",
            List.of(new BranchEntity("B1","New",new ArrayList<>())));
        Franchise dto = new Franchise("F1","Fr1",
            List.of(new Branch("B1","New",new ArrayList<>())));
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        given(repo.save(any())).willReturn(Mono.just(upd));
        doReturn(dto).when(mapper).map(upd,Franchise.class);

        StepVerifier.create(service.renameBranch("F1","B1","New"))
                    .expectNext(dto).verifyComplete();
    }

    @Test
    void renameProduct_success_path() {
        BranchEntity b = new BranchEntity("B1","S1",
            List.of(new ProductEntity("P1","Old",1)));
        baseEntity.setBranches(List.of(b));
        FranchiseEntity upd = new FranchiseEntity("F1","Fr1",
            List.of(new BranchEntity("B1","S1",
                List.of(new ProductEntity("P1","New",1)))));
        Franchise dto = new Franchise("F1","Fr1",
            List.of(new Branch("B1","S1",
                List.of(new Product("P1","New",1)))));
        given(repo.findById("F1")).willReturn(Mono.just(baseEntity));
        given(repo.save(any())).willReturn(Mono.just(upd));
        doReturn(dto).when(mapper).map(upd,Franchise.class);

        StepVerifier.create(service.renameProduct("F1","B1","P1","New"))
                    .expectNext(dto).verifyComplete();
    }
}
