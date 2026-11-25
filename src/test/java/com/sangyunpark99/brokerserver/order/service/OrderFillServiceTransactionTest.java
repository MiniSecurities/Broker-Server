package com.sangyunpark99.brokerserver.order.service;

import com.sangyunpark99.brokerserver.fill.Fill;
import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class OrderFillServiceTransactionTest {

    @Autowired
    private OrderFillService orderFillService;

    @MockitoSpyBean
    private FillRepository fillRepository;

    @AfterEach
    void tearDown() {
        fillRepository.deleteAll();
    }

    @Test
    @DisplayName("체결 계획을 저장하다 실패하면 앞서 저장한 계획까지 모두 남지 않는다")
    void rollsBackWholePlan() {
        AtomicInteger saves = new AtomicInteger();
        doAnswer(invocation -> {
            if (saves.incrementAndGet() == 2) {
                throw new IllegalStateException("저장 중 장애");
            }
            return invocation.callRealMethod();
        }).when(fillRepository).save(any(Fill.class));

        assertThatThrownBy(() -> orderFillService.accept(new BrokerOrderRequest(7L, "AAPL", 1_000_000L, new BigDecimal("200"))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fillRepository.count()).isZero();
    }
}
