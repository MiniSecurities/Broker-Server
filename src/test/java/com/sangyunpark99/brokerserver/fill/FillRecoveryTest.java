package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.client.FillNotification;
import com.sangyunpark99.brokerserver.order.client.TradeServerClient;
import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import com.sangyunpark99.brokerserver.order.service.OrderFillService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class FillRecoveryTest {

    private static final long QUANTITY = 1_000_000L;

    @Autowired
    private OrderFillService orderFillService;

    @Autowired
    private FillRepository fillRepository;

    @MockitoBean
    private TradeServerClient tradeServerClient;

    @BeforeEach
    void setUp() {
        when(tradeServerClient.isHealthy()).thenReturn(true);
        when(tradeServerClient.sendFill(any())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        fillRepository.deleteAll();
    }

    private void awaitAllSent() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            List<Fill> fills = fillRepository.findAll();
            if (!fills.isEmpty() && fills.stream().allMatch(fill -> fill.getStatus() == FillStatus.SENT)) {
                return;
            }
            Thread.sleep(200);
        }
    }

    @Test
    @DisplayName("접수 직후 체결 계획 전부가 DB에 SCHEDULED로 남고, 스케줄러가 DB만 보고 끝까지 체결·통보한다")
    void planIsDurableAndSchedulerFinishesIt() throws InterruptedException {
        orderFillService.accept(new BrokerOrderRequest(7L, "AAPL", QUANTITY, new BigDecimal("200")));

        List<Fill> plan = fillRepository.findAll();
        assertThat(plan).isNotEmpty();
        assertThat(plan).allSatisfy(fill -> assertThat(fill.getStatus()).isEqualTo(FillStatus.SCHEDULED));
        assertThat(plan.stream().mapToLong(Fill::getQuantity).sum()).isEqualTo(QUANTITY);

        awaitAllSent();

        List<Fill> fills = fillRepository.findAll();
        assertThat(fills).hasSize(plan.size());
        assertThat(fills).allSatisfy(fill -> assertThat(fill.getStatus()).isEqualTo(FillStatus.SENT));

        ArgumentCaptor<FillNotification> sent = ArgumentCaptor.forClass(FillNotification.class);
        verify(tradeServerClient, atLeastOnce()).sendFill(sent.capture());
        assertThat(sent.getAllValues()).extracting(FillNotification::fillId)
                .containsAll(plan.stream().map(Fill::getFillId).toList());
        assertThat(sent.getAllValues().stream().distinct().mapToLong(FillNotification::fillQuantity).sum())
                .isEqualTo(QUANTITY);
    }
}
