package com.sangyunpark99.brokerserver.order.service;

import com.sangyunpark99.brokerserver.fill.Fill;
import com.sangyunpark99.brokerserver.fill.FillStatus;
import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import com.sangyunpark99.brokerserver.order.utils.FillPricePicker;
import com.sangyunpark99.brokerserver.order.utils.FillQuantitySplitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderFillServiceTest {

    private final FillRepository fillRepository = mock(FillRepository.class);
    private final OrderFillService service = new OrderFillService(
            new FillQuantitySplitter(), new FillPricePicker(), fillRepository);

    @BeforeEach
    void setUp() {
        when(fillRepository.save(any(Fill.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private List<Fill> savedFills() {
        ArgumentCaptor<Fill> saved = ArgumentCaptor.forClass(Fill.class);
        verify(fillRepository, atLeastOnce()).save(saved.capture());
        return saved.getAllValues();
    }

    @Test
    @DisplayName("주문을 받으면 체결 계획을 모두 SCHEDULED로 바로 저장한다")
    void savesWholePlanAsScheduled() {
        BigDecimal limitPrice = new BigDecimal("200");

        service.accept(new BrokerOrderRequest(7L, "AAPL", 10L, limitPrice));

        List<Fill> fills = savedFills();
        assertThat(fills).allSatisfy(fill -> {
            assertThat(fill.getOrderId()).isEqualTo(7L);
            assertThat(fill.getStatus()).isEqualTo(FillStatus.SCHEDULED);
            assertThat(fill.getPrice()).isLessThanOrEqualTo(limitPrice);
            assertThat(fill.getPrice().signum()).isPositive();
        });
        assertThat(fills.stream().mapToLong(Fill::getQuantity).sum()).isEqualTo(10L);
        assertThat(fills).extracting(Fill::getFillId).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("체결 예정 시각은 접수 시각부터 0.5초 간격으로 정해진다")
    void fillAtIsEveryHalfSecond() {
        LocalDateTime before = LocalDateTime.now();

        service.accept(new BrokerOrderRequest(7L, "AAPL", 1000L, new BigDecimal("200")));

        LocalDateTime after = LocalDateTime.now();
        List<Fill> fills = savedFills();
        assertThat(fills.get(0).getFillAt()).isBetween(before.plusNanos(500_000_000), after.plusNanos(500_000_000));
        for (int i = 1; i < fills.size(); i++) {
            assertThat(Duration.between(fills.get(i - 1).getFillAt(), fills.get(i).getFillAt()))
                    .isEqualTo(Duration.ofMillis(500));
        }
    }
}
