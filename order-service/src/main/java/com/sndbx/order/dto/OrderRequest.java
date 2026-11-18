package com.sndbx.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(
        @NotBlank @Email String email,
        @NotNull List<Product> products,
        String comment
) {
    public record Product(
            @NotBlank String productId,
            @NotNull @Positive Integer quantity
    ) {
    }
}


//public record OrderRequest(
//        @NotBlank @Email String email,
//        @NotBlank String productId,
//        @NotNull @Positive Integer quantity,
//        String comment
//) {
//}


//public record OrderRequest(
//        @NotBlank
//        String productId,
//        @Min(1)
//        Integer quantity
//) {}

//public record OrderResponse(
//        Long id,
//        String orderNumber,
//        String productId,
//        BigDecimal price,
//        Integer quantity,
//        UserDetails userDetails
//) {}
