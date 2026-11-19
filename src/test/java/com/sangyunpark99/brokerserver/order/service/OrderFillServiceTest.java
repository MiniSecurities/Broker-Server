package com.sangyunpark99.brokerserver.order.service;

import com.sangyunpark99.brokerserver.order.client.FillNotification;
import com.sangyunpark99.brokerserver.order.client.TradeServerClient;
import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import com.sangyunpark99.brokerserver.order.utils.FillPricePicker;
import com.sangyunpark99.brokerserver.order.utils.FillQuantitySplitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFillServiceTest {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final RecordingTradeServerClient client = new RecordingTradeServerClient();
    private final OrderFillService service = new OrderFillService(
            new FillQuantitySplitter(), new FillPricePicker(), scheduler, client);

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    @DisplayName("accept는 체결을 기다리지 않고 바로 돌아온다")
    void returnsImmediately() {
        long start = System.nanoTime();

        service.accept(new BrokerOrderRequest(1L, "AAPL", 10L, new BigDecimal("200")));

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(elapsedMillis).isLessThan(100);
        assertThat(client.sent).isEmpty();
    }

    @Test
    @DisplayName("체결 통보 수량을 모두 더하면 주문 수량이고, 체결가는 모두 지정가 이하다")
    void sendsAllFills() throws InterruptedException {
        BigDecimal limitPrice = new BigDecimal("200");

        service.accept(new BrokerOrderRequest(7L, "AAPL", 10L, limitPrice));
        scheduler.shutdown();
        assertThat(scheduler.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(client.sent).isNotEmpty()
                .allSatisfy(n -> {
                    assertThat(n.orderId()).isEqualTo(7L);
                    assertThat(n.fillPrice()).isLessThanOrEqualTo(limitPrice);
                    assertThat(n.fillPrice().signum()).isPositive();
                });
        assertThat(client.sent.stream().mapToLong(FillNotification::fillQuantity).sum()).isEqualTo(10L);
    }

    static class RecordingTradeServerClient extends TradeServerClient {

        private final List<FillNotification> sent = new CopyOnWriteArrayList<>();

        RecordingTradeServerClient() {
            super(null);
        }

        @Override
        public void sendFill(FillNotification notification) {
            sent.add(notification);
        }
    }
}
