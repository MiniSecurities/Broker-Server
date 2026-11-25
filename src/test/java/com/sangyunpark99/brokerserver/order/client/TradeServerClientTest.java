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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TradeServerClientTest {

    private static final FillNotification NOTIFICATION =
            new FillNotification("fill-1", 1L, 3L, new BigDecimal("197.42"));

    private MockRestServiceServer server;
    private TradeServerClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://trade");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TradeServerClient(builder.build());
    }

    @Test
    @DisplayName("체결 ID를 포함한 체결을 POST /api/fills로 보내고 성공하면 true")
    void sendsFill() {
        server.expect(requestTo("http://trade/api/fills"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"fillId": "fill-1", "orderId": 1, "fillQuantity": 3, "fillPrice": 197.42}
                        """))
                .andRespond(withSuccess());

        assertThat(client.sendFill(NOTIFICATION)).isTrue();

        server.verify();
    }

    @Test
    @DisplayName("이미 받은 체결이라 204가 와도 true")
    void duplicateFillIsSuccess() {
        server.expect(requestTo("http://trade/api/fills"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertThat(client.sendFill(NOTIFICATION)).isTrue();

        server.verify();
    }

    @Test
    @DisplayName("Trade-Server가 500을 돌려주면 예외 없이 false")
    void serverErrorIsFailure() {
        server.expect(requestTo("http://trade/api/fills"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(client.sendFill(NOTIFICATION)).isFalse();

        server.verify();
    }

    @Test
    @DisplayName("헬스 체크가 200이면 true")
    void healthy() {
        server.expect(requestTo("http://trade/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        assertThat(client.isHealthy()).isTrue();

        server.verify();
    }

    @Test
    @DisplayName("헬스 체크가 503(DOWN)이면 예외 없이 false")
    void unhealthy() {
        server.expect(requestTo("http://trade/actuator/health"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThat(client.isHealthy()).isFalse();

        server.verify();
    }
}
