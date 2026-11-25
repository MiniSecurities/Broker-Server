package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FillSchedulerTest {

    private final FillRepository fillRepository = mock(FillRepository.class);
    private final FillSender fillSender = mock(FillSender.class);
    private final FillScheduler fillScheduler = new FillScheduler(fillRepository, fillSender);

    private static Fill scheduled(String fillId) {
        return new Fill(fillId, 7L, 1, new BigDecimal("100"), LocalDateTime.now().minusSeconds(1));
    }

    @Test
    @DisplayName("체결 시각이 된 계획을 PENDING으로 저장한 뒤 통보한다")
    void fillsThenSends() {
        Fill first = scheduled("fill-1");
        Fill second = scheduled("fill-2");
        when(fillRepository.findTop100ByStatusAndFillAtLessThanEqualOrderByIdAsc(eq(FillStatus.SCHEDULED), any()))
                .thenReturn(List.of(first, second));

        LocalDateTime before = LocalDateTime.now();
        fillScheduler.fill();
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> now = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(fillRepository).findTop100ByStatusAndFillAtLessThanEqualOrderByIdAsc(eq(FillStatus.SCHEDULED), now.capture());
        assertThat(now.getValue()).isBetween(before, after);

        assertThat(first.getStatus()).isEqualTo(FillStatus.PENDING);
        assertThat(second.getStatus()).isEqualTo(FillStatus.PENDING);

        InOrder inOrder = inOrder(fillRepository, fillSender);
        inOrder.verify(fillRepository).save(first);
        inOrder.verify(fillSender).send(first);
        inOrder.verify(fillRepository).save(second);
        inOrder.verify(fillSender).send(second);
    }

    @Test
    @DisplayName("체결 시각이 된 계획이 없으면 저장도 통보도 하지 않는다")
    void doesNothingWithoutDueFills() {
        when(fillRepository.findTop100ByStatusAndFillAtLessThanEqualOrderByIdAsc(eq(FillStatus.SCHEDULED), any()))
                .thenReturn(List.of());

        fillScheduler.fill();

        verify(fillRepository, never()).save(any());
        verify(fillSender, never()).send(any());
    }
}
