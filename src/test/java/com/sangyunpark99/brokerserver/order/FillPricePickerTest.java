package com.sangyunpark99.brokerserver.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FillPricePickerTest {

    private final FillPricePicker picker = new FillPricePicker();

    @ParameterizedTest
    @ValueSource(strings = {"200", "199.99", "1.00", "123.4567", "100000"})
    @DisplayName("체결가는 지정가의 98% 이상, 지정가 이하다")
    void withinRange(String limit) {
        BigDecimal limitPrice = new BigDecimal(limit);
        BigDecimal lowest = limitPrice.multiply(new BigDecimal("0.98")).subtract(new BigDecimal("0.01"));

        for (int i = 0; i < 1_000; i++) {
            BigDecimal fillPrice = picker.pick(limitPrice);

            assertThat(fillPrice).isLessThanOrEqualTo(limitPrice);
            assertThat(fillPrice).isGreaterThanOrEqualTo(lowest);
        }
    }

    @Test
    @DisplayName("체결가는 소수 둘째 자리까지다")
    void scaleIsTwo() {
        for (int i = 0; i < 1_000; i++) {
            assertThat(picker.pick(new BigDecimal("123.4567")).scale()).isEqualTo(2);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "0.005"})
    @DisplayName("지정가가 아주 작아도 체결가는 0보다 크고 지정가 이하다")
    void tinyLimitPriceIsStillPositive(String limit) {
        BigDecimal limitPrice = new BigDecimal(limit);

        for (int i = 0; i < 1_000; i++) {
            BigDecimal fillPrice = picker.pick(limitPrice);

            assertThat(fillPrice.signum()).isPositive();
            assertThat(fillPrice).isLessThanOrEqualTo(limitPrice);
        }
    }

    @Test
    @DisplayName("반복하면 지정가보다 낮은 체결가가 나온다")
    void sometimesBelowLimit() {
        BigDecimal limitPrice = new BigDecimal("200");
        boolean below = false;
        for (int i = 0; i < 1_000 && !below; i++) {
            below = picker.pick(limitPrice).compareTo(limitPrice) < 0;
        }

        assertThat(below).isTrue();
    }
}
