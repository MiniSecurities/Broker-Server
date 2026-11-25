package com.sangyunpark99.brokerserver.fill.repository;

import com.sangyunpark99.brokerserver.fill.Fill;
import com.sangyunpark99.brokerserver.fill.FillStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FillRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 25, 10, 0, 0);

    @Autowired
    private FillRepository fillRepository;

    private void save(String fillId, LocalDateTime fillAt, FillStatus status) {
        Fill fill = new Fill(fillId, 7L, 1, new BigDecimal("100"), fillAt);
        if (status != FillStatus.SCHEDULED) {
            fill.markFilled();
        }
        if (status == FillStatus.SENT) {
            fill.markSent();
        }
        fillRepository.save(fill);
    }

    @Test
    @DisplayName("체결 시각이 된 SCHEDULED만 먼저 계획된 순서로 가져온다")
    void findsDueScheduledFills() {
        save("due-1", NOW.minusSeconds(2), FillStatus.SCHEDULED);
        save("future", NOW.plusSeconds(1), FillStatus.SCHEDULED);
        save("pending", NOW.minusSeconds(2), FillStatus.PENDING);
        save("due-now", NOW, FillStatus.SCHEDULED);

        List<Fill> fills = fillRepository.findTop100ByStatusAndFillAtLessThanEqualOrderByIdAsc(
                FillStatus.SCHEDULED, NOW);

        assertThat(fills).extracting(Fill::getFillId).containsExactly("due-1", "due-now");
    }

    @Test
    @DisplayName("체결된 지 기준 시각보다 오래된 PENDING만 오래된 순서로 가져온다")
    void findsOldPendingFills() {
        save("old-pending-1", NOW.minusSeconds(10), FillStatus.PENDING);
        save("old-sent", NOW.minusSeconds(10), FillStatus.SENT);
        save("old-scheduled", NOW.minusSeconds(10), FillStatus.SCHEDULED);
        save("recent-pending", NOW.minusSeconds(1), FillStatus.PENDING);
        save("old-pending-2", NOW.minusSeconds(5), FillStatus.PENDING);

        List<Fill> fills = fillRepository.findTop100ByStatusAndFillAtBeforeOrderByIdAsc(
                FillStatus.PENDING, NOW.minusSeconds(3));

        assertThat(fills).extracting(Fill::getFillId).containsExactly("old-pending-1", "old-pending-2");
    }
}
