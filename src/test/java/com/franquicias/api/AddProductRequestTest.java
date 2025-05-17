package com.franquicias.api;

import com.franquicias.api.dto.AddProductRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AddProductRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_noConstraintViolations() {
        AddProductRequest dto = new AddProductRequest();
        dto.setId("P1");
        dto.setName("Producto");
        dto.setStock(10);

        Set<ConstraintViolation<AddProductRequest>> violations =
            validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenStockNegative_violationOnStock() {
        AddProductRequest dto = new AddProductRequest("P1", "X", -5);

        Set<ConstraintViolation<AddProductRequest>> violations =
            validator.validate(dto);

        assertThat(violations)
            .extracting(v -> v.getPropertyPath().toString())
            .containsExactly("stock");
    }
}
