package com.sangyunpark99.brokerserver.order.service;

import com.sangyunpark99.brokerserver.fill.Fill;
import com.sangyunpark99.brokerserver.fill.FillSender;
import com.sangyunpark99.brokerserver.fill.FillStatus;
import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import com.sangyunpark99.brokerserver.order.utils.FillPricePicker;
import com.sangyunpark99.brokerserver.order.utils.FillQuantitySplitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderFillServiceTest {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final FillRepository fillRepository = mock(FillRepository.class);
    private final FillSender fillSender = mock(FillSender.class);
    private final OrderFillService service = new OrderFillService(
            new FillQuantitySplitter(), new FillPricePicker(), fillRepository, scheduler, fillSender);

    @BeforeEach
    void setUp() {
        when(fillRepository.save(any(Fill.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    @DisplayName("accept는 체결을 기다리지 않고 바로 돌아온다")
    void returnsImmediately() {
        long start = System.nanoTime();

        service.accept(new BrokerOrderRequest(1L, "AAPL", 10L, new BigDecimal("200")));

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(elapsedMillis).isLessThan(100);
        verify(fillRepository, never()).save(any());
        verify(fillSender, never()).send(any());
    }

    @Test
    @DisplayName("체결을 PENDING으로 저장한 뒤 바로 보내고, 수량 합은 주문 수량이며 체결가는 지정가 이하다")
    void savesThenSendsAllFills() throws InterruptedException {
        BigDecimal limitPrice = new BigDecimal("200");

        service.accept(new BrokerOrderRequest(7L, "AAPL", 10L, limitPrice));
        scheduler.shutdown();
        assertThat(scheduler.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        ArgumentCaptor<Fill> saved = ArgumentCaptor.forClass(Fill.class);
        verify(fillRepository, atLeastOnce()).save(saved.capture());
        List<Fill> fills = saved.getAllValues();

        assertThat(fills).allSatisfy(fill -> {
            assertThat(fill.getOrderId()).isEqualTo(7L);
            assertThat(fill.getStatus()).isEqualTo(FillStatus.PENDING);
            assertThat(fill.getPrice()).isLessThanOrEqualTo(limitPrice);
            assertThat(fill.getPrice().signum()).isPositive();
        });
        assertThat(fills.stream().mapToLong(Fill::getQuantity).sum()).isEqualTo(10L);
        assertThat(fills).extracting(Fill::getFillId).doesNotHaveDuplicates();

        ArgumentCaptor<Fill> sent = ArgumentCaptor.forClass(Fill.class);
        verify(fillSender, atLeastOnce()).send(sent.capture());
        assertThat(sent.getAllValues()).containsExactlyInAnyOrderElementsOf(fills);
    }
}
