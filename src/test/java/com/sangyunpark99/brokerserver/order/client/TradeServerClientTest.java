package com.sangyunpark99.brokerserver.order.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class TradeServerClientTest {

    private MockRestServiceServer server;
    private TradeServerClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://trade");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TradeServerClient(builder.build());
    }

    @Test
    @DisplayName("체결을 POST /api/fills로 보낸다")
    void sendsFill() {
        server.expect(requestTo("http://trade/api/fills"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"orderId": 1, "fillQuantity": 3, "fillPrice": 197.42}
                        """))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.sendFill(new FillNotification(1L, 3L, new BigDecimal("197.42")));

        server.verify();
    }

    @Test
    @DisplayName("Trade-Server가 500을 돌려줘도 예외를 밖으로 던지지 않는다")
    void doesNotThrowOnServerError() {
        server.expect(requestTo("http://trade/api/fills"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatCode(() -> client.sendFill(new FillNotification(1L, 3L, new BigDecimal("197.42"))))
                .doesNotThrowAnyException();

        server.verify();
    }
}
