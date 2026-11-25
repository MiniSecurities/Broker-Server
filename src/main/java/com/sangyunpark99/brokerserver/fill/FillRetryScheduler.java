package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.client.TradeServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FillRetryScheduler {

    private final TradeServerClient tradeServerClient;
    private final FillRepository fillRepository;
    private final FillSender fillSender;

    @Scheduled(fixedDelay = 1000)
    public void retry() {

        if(!tradeServerClient.isHealthy()) return;

        List<Fill> fills = fillRepository.findTop100ByStatusAndFillAtBeforeOrderByIdAsc(FillStatus.PENDING, LocalDateTime.now().minusSeconds(3));

        for(Fill fill: fills) {
            fillSender.send(fill);
        }
    }
}
