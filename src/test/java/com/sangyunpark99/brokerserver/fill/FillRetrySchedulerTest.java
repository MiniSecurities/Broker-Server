package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.client.TradeServerClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FillRetrySchedulerTest {

    private final TradeServerClient tradeServerClient = mock(TradeServerClient.class);
    private final FillRepository fillRepository = mock(FillRepository.class);
    private final FillSender fillSender = mock(FillSender.class);
    private final FillRetryScheduler scheduler = new FillRetryScheduler(tradeServerClient, fillRepository, fillSender);

    @Test
    @DisplayName("거래 서버가 죽어 있으면 DB를 조회하지 않고 아무것도 보내지 않는다")
    void skipsWhenTradeServerIsDown() {
        when(tradeServerClient.isHealthy()).thenReturn(false);

        scheduler.retry();

        verifyNoInteractions(fillRepository, fillSender);
    }

    @Test
    @DisplayName("거래 서버가 살아 있으면 3초 넘게 PENDING인 체결을 하나씩 다시 보낸다")
    void resendsOldPendingFills() {
        when(tradeServerClient.isHealthy()).thenReturn(true);
        Fill first = new Fill("fill-1", 7L, 3, new BigDecimal("99699"), LocalDateTime.now());
        Fill second = new Fill("fill-2", 7L, 7, new BigDecimal("99500"), LocalDateTime.now());
        when(fillRepository.findTop100ByStatusAndFillAtBeforeOrderByIdAsc(eq(FillStatus.PENDING), any()))
                .thenReturn(List.of(first, second));

        LocalDateTime before = LocalDateTime.now();
        scheduler.retry();
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(fillRepository).findTop100ByStatusAndFillAtBeforeOrderByIdAsc(eq(FillStatus.PENDING), cutoff.capture());
        assertThat(cutoff.getValue()).isBetween(before.minusSeconds(3), after.minusSeconds(3));
        verify(fillSender).send(first);
        verify(fillSender).send(second);
    }

    @Test
    @DisplayName("다시 보낼 체결이 없으면 아무것도 보내지 않는다")
    void doesNothingWithoutPendingFills() {
        when(tradeServerClient.isHealthy()).thenReturn(true);
        when(fillRepository.findTop100ByStatusAndFillAtBeforeOrderByIdAsc(eq(FillStatus.PENDING), any()))
                .thenReturn(List.of());

        scheduler.retry();

        verify(fillSender, never()).send(any());
    }
}
