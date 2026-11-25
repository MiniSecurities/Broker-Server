package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
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
import static org.mockito.Mockito.when;

class FillRetrySchedulerTest {

    private final FillRepository fillRepository = mock(FillRepository.class);
    private final FillSender fillSender = mock(FillSender.class);
    private final FillRetryScheduler scheduler = new FillRetryScheduler(fillRepository, fillSender);

    @Test
    @DisplayName("3초 넘게 PENDING인 체결을 하나씩 다시 보낸다")
    void resendsOldPendingFills() {
        Fill first = new Fill("fill-1", 7L, 3, new BigDecimal("99699"));
        Fill second = new Fill("fill-2", 7L, 7, new BigDecimal("99500"));
        when(fillRepository.findTop100ByStatusAndCreatedAtBeforeOrderByIdAsc(eq(FillStatus.PENDING), any()))
                .thenReturn(List.of(first, second));

        LocalDateTime before = LocalDateTime.now();
        scheduler.retry();
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(fillRepository).findTop100ByStatusAndCreatedAtBeforeOrderByIdAsc(eq(FillStatus.PENDING), cutoff.capture());
        assertThat(cutoff.getValue()).isBetween(before.minusSeconds(3), after.minusSeconds(3));
        verify(fillSender).send(first);
        verify(fillSender).send(second);
    }

    @Test
    @DisplayName("다시 보낼 체결이 없으면 아무것도 보내지 않는다")
    void doesNothingWithoutPendingFills() {
        when(fillRepository.findTop100ByStatusAndCreatedAtBeforeOrderByIdAsc(eq(FillStatus.PENDING), any()))
                .thenReturn(List.of());

        scheduler.retry();

        verify(fillSender, never()).send(any());
    }
}
