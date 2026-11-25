package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.client.FillNotification;
import com.sangyunpark99.brokerserver.order.client.TradeServerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FillSender {

    private final TradeServerClient tradeServerClient;
    private final FillRepository fillRepository;

    public void send(Fill fill) {
        FillNotification fillNotification = new FillNotification(fill.getFillId(), fill.getOrderId(), fill.getQuantity(), fill.getPrice());
        boolean result = tradeServerClient.sendFill(fillNotification);

        if(result) {
            fill.markSent();
            fillRepository.save(fill);
        }
    }
}