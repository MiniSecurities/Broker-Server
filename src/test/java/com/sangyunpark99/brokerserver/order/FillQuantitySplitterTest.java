package com.sangyunpark99.brokerserver.order;

import com.sangyunpark99.brokerserver.order.utils.FillQuantitySplitter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FillQuantitySplitterTest {

    private final FillQuantitySplitter splitter = new FillQuantitySplitter();

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 10, 100, 10_000})
    @DisplayName("조각을 모두 더하면 주문 수량과 같다")
    void sumEqualsQuantity(long quantity) {
        for (int i = 0; i < 1_000; i++) {
            List<Long> fillQuantities = splitter.split(quantity);

            assertThat(fillQuantities.stream().mapToLong(Long::longValue).sum()).isEqualTo(quantity);
        }
    }

    @RepeatedTest(1_000)
    @DisplayName("모든 조각은 1 이상이고 주문 수량 이하다")
    void everyPieceIsWithinRange() {
        List<Long> fillQuantities = splitter.split(10);

        assertThat(fillQuantities).isNotEmpty()
                .allSatisfy(q -> assertThat(q).isBetween(1L, 10L));
    }

    @Test
    @DisplayName("수량이 1이면 조각은 1 하나다")
    void singleShare() {
        assertThat(splitter.split(1)).containsExactly(1L);
    }

    @Test
    @DisplayName("반복하면 여러 조각으로 나뉘는 경우가 생긴다")
    void sometimesSplitsIntoMultiplePieces() {
        boolean split = false;
        for (int i = 0; i < 1_000 && !split; i++) {
            split = splitter.split(10).size() > 1;
        }

        assertThat(split).isTrue();
    }
}
