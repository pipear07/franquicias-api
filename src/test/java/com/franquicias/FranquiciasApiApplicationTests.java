package com.franquicias;

import com.franquicias.config.ModelMapperConfig;

import com.franquicias.api.FranchiseController;
import com.franquicias.domain.service.FranchiseService;
import com.franquicias.infrastructure.db.MongoFranchiseAdapter;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class FranquiciasApiApplicationTests {

    @Autowired
    ApplicationContext ctx;

    @Autowired
    FranchiseController controller;

    @Autowired
    FranchiseService service;

    @Autowired
    MongoFranchiseAdapter adapter;

    @Autowired
    ModelMapper modelMapper;            // injects the bean that comes from ModelMapper Config

    @Autowired
    ModelMapperConfig configBean;       

    @Test
    void contextLoads() {
        // The context starts without exceptions
    }

    @Test
    void allPrimaryBeansArePresent() {
        assertThat(ctx.containsBean("franchiseController")).isTrue();
        assertThat(ctx.containsBean("franchiseService")).isTrue();
        assertThat(ctx.containsBean("mongoFranchiseAdapter")).isTrue();
        assertThat(ctx.containsBean("modelMapper")).isTrue();
        assertThat(ctx.containsBean("modelMapperConfig")).isTrue(); // configuration bean name
    }

    @Test
    void beansAreNonNull() {
        assertThat(controller).isNotNull();
        assertThat(service).isNotNull();
        assertThat(adapter).isNotNull();
        assertThat(modelMapper).isNotNull();
        assertThat(configBean).isNotNull();
    }

    @Test
    void main_doesNotThrow() {
        assertThatCode(() -> FranquiciasApiApplication.main(new String[]{}))
            .doesNotThrowAnyException();
    }
}
