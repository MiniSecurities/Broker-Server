package com.sangyunpark99.brokerserver.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BrokerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions send(String body) throws Exception {
        return mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    @DisplayName("주문을 받으면 202를 돌려준다")
    void acceptsOrder() throws Exception {
        send("""
                {"orderId": 1, "symbol": "AAPL", "quantity": 10, "limitPrice": 200}
                """)
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("주문 ID가 없으면 400")
    void missingOrderId() throws Exception {
        send("""
                {"symbol": "AAPL", "quantity": 10, "limitPrice": 200}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수량이 0이면 400")
    void zeroQuantity() throws Exception {
        send("""
                {"orderId": 1, "symbol": "AAPL", "quantity": 0, "limitPrice": 200}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("지정가가 없으면 400")
    void missingLimitPrice() throws Exception {
        send("""
                {"orderId": 1, "symbol": "AAPL", "quantity": 10}
                """)
                .andExpect(status().isBadRequest());
    }
}
