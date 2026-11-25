package com.sangyunpark99.brokerserver.order.service;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import com.sangyunpark99.brokerserver.order.utils.FillPricePicker;
import com.sangyunpark99.brokerserver.order.utils.FillQuantitySplitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class OrderFillServiceTransactionTest {

    @Autowired
    private OrderFillService orderFillService;

    @Autowired
    private FillRepository fillRepository;

    @MockitoBean
    private FillQuantitySplitter fillQuantitySplitter;

    @MockitoBean
    private FillPricePicker fillPricePicker;

    @AfterEach
    void tearDown() {
        fillRepository.deleteAll();
    }

    @Test
    @DisplayName("체결 계획을 저장하다 실패하면 앞서 저장한 계획까지 모두 남지 않는다")
    void rollsBackWholePlan() {
        given(fillQuantitySplitter.split(anyLong())).willReturn(List.of(3L, 7L));
        given(fillPricePicker.pick(any()))
                .willReturn(new BigDecimal("199"))
                .willThrow(new IllegalStateException("두 번째 체결 계획 중 장애"));

        assertThatThrownBy(() -> orderFillService.accept(new BrokerOrderRequest(7L, "AAPL", 10L, new BigDecimal("200"))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fillRepository.count()).isZero();
    }
}
