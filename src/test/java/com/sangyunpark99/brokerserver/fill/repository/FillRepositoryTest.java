package com.sangyunpark99.brokerserver.fill.repository;

import com.sangyunpark99.brokerserver.fill.Fill;
import com.sangyunpark99.brokerserver.fill.FillStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FillRepositoryTest {

    @Autowired
    private FillRepository fillRepository;

    private Fill save(String fillId, LocalDateTime createdAt, boolean sent) {
        Fill fill = new Fill(fillId, 7L, 1, new BigDecimal("100"));
        ReflectionTestUtils.setField(fill, "createdAt", createdAt);
        if (sent) {
            fill.markSent();
        }
        return fillRepository.save(fill);
    }

    @Test
    @DisplayName("기준 시각보다 먼저 생긴 PENDING 체결만 오래된 순서로 가져온다")
    void findsOldPendingFills() {
        LocalDateTime now = LocalDateTime.now();
        save("old-pending-1", now.minusSeconds(10), false);
        save("old-sent", now.minusSeconds(10), true);
        save("new-pending", now, false);
        save("old-pending-2", now.minusSeconds(5), false);

        List<Fill> fills = fillRepository.findTop100ByStatusAndCreatedAtBeforeOrderByIdAsc(
                FillStatus.PENDING, now.minusSeconds(3));

        assertThat(fills).extracting(Fill::getFillId).containsExactly("old-pending-1", "old-pending-2");
    }
}
