package com.sangyunpark99.brokerserver.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeServerClient {

    private final RestClient tradeServerRestClient;

    public boolean sendFill(FillNotification notification) {
        try {
            tradeServerRestClient.post()
                    .uri("/api/fills")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(notification)
                    .retrieve()
                    .toBodilessEntity();
            log.info("체결 통보 전송 fillId={} orderId={} quantity={} price={}",
                    notification.fillId(), notification.orderId(), notification.fillQuantity(), notification.fillPrice());

            return true;
        } catch (RestClientException e) {
            log.error("체결 통보 전송 실패 fillId={} orderId={} quantity={} price={}",
                    notification.fillId(), notification.orderId(), notification.fillQuantity(), notification.fillPrice(), e);

            return false;
        }
    }
}
