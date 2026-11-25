package com.sangyunpark99.brokerserver.fill;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FillTest {

    private static final LocalDateTime FILL_AT = LocalDateTime.of(2026, 11, 25, 10, 0, 0);

    private static Fill fill() {
        return new Fill("fill-1", 7L, 3, new BigDecimal("99699"), FILL_AT);
    }

    @Test
    @DisplayName("체결 계획은 SCHEDULED로 시작하고 체결 예정 시각을 가진다")
    void startsScheduled() {
        Fill fill = fill();

        assertThat(fill.getStatus()).isEqualTo(FillStatus.SCHEDULED);
        assertThat(fill.getFillAt()).isEqualTo(FILL_AT);
        assertThat(fill.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("체결이 발생하면 PENDING이 된다")
    void markFilled() {
        Fill fill = fill();

        fill.markFilled();

        assertThat(fill.getStatus()).isEqualTo(FillStatus.PENDING);
    }

    @Test
    @DisplayName("체결된 뒤 통보에 성공하면 SENT가 된다")
    void markSentAfterFilled() {
        Fill fill = fill();
        fill.markFilled();

        fill.markSent();

        assertThat(fill.getStatus()).isEqualTo(FillStatus.SENT);
    }

    @Test
    @DisplayName("아직 체결되지 않은 계획은 통보 완료로 바뀌지 않는다")
    void cannotMarkSentBeforeFilled() {
        Fill fill = fill();

        fill.markSent();

        assertThat(fill.getStatus()).isEqualTo(FillStatus.SCHEDULED);
    }

    @Test
    @DisplayName("통보가 끝난 체결은 다시 PENDING으로 돌아가지 않는다")
    void cannotMarkFilledAfterSent() {
        Fill fill = fill();
        fill.markFilled();
        fill.markSent();

        fill.markFilled();

        assertThat(fill.getStatus()).isEqualTo(FillStatus.SENT);
    }
}
