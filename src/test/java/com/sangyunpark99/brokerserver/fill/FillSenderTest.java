package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.client.FillNotification;
import com.sangyunpark99.brokerserver.order.client.TradeServerClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FillSenderTest {

    private final TradeServerClient tradeServerClient = mock(TradeServerClient.class);
    private final FillRepository fillRepository = mock(FillRepository.class);
    private final FillSender fillSender = new FillSender(tradeServerClient, fillRepository);

    private static Fill fill() {
        return new Fill("fill-1", 7L, 3, new BigDecimal("99699"));
    }

    @Test
    @DisplayName("전송에 성공하면 SENT로 바꿔 저장한다")
    void marksSentOnSuccess() {
        Fill fill = fill();
        when(tradeServerClient.sendFill(any())).thenReturn(true);

        fillSender.send(fill);

        assertThat(fill.getStatus()).isEqualTo(FillStatus.SENT);
        verify(fillRepository).save(fill);
    }

    @Test
    @DisplayName("체결 내용 그대로 통보한다")
    void sendsFillContent() {
        when(tradeServerClient.sendFill(any())).thenReturn(true);

        fillSender.send(fill());

        verify(tradeServerClient).sendFill(new FillNotification("fill-1", 7L, 3L, new BigDecimal("99699")));
    }

    @Test
    @DisplayName("전송에 실패하면 PENDING으로 두고 저장하지 않는다")
    void keepsPendingOnFailure() {
        Fill fill = fill();
        when(tradeServerClient.sendFill(any())).thenReturn(false);

        fillSender.send(fill);

        assertThat(fill.getStatus()).isEqualTo(FillStatus.PENDING);
        verify(fillRepository, never()).save(any());
    }
}
