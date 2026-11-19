package com.sangyunpark99.brokerserver.order.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BrokerOrderRequest(

        @NotNull
        @Positive
        Long orderId,

        @NotBlank
        String symbol,

        @NotNull
        @Positive
        Long quantity,

        @NotNull
        @Positive
        BigDecimal limitPrice
) {
}
