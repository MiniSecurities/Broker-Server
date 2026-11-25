package com.sangyunpark99.brokerserver.order.service;

import com.sangyunpark99.brokerserver.fill.Fill;
import com.sangyunpark99.brokerserver.fill.FillSender;
import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import com.sangyunpark99.brokerserver.order.utils.FillPricePicker;
import com.sangyunpark99.brokerserver.order.utils.FillQuantitySplitter;
import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderFillService {

    private final FillQuantitySplitter fillQuantitySplitter;
    private final FillPricePicker fillPricePicker;
    private final FillRepository fillRepository;
    private final ScheduledExecutorService scheduler;
    private final FillSender fillSender;

    public void accept(BrokerOrderRequest request) {
        List<Long> fillQuantities = fillQuantitySplitter.split(request.quantity());

        long delay = 0;

        for(Long fillQuantity: fillQuantities) {
            delay += 500;
            BigDecimal fillPrice = fillPricePicker.pick(request.limitPrice());
            String fillId = String.valueOf(UUID.randomUUID());
            scheduler.schedule(() -> {
                    Fill fill = fillRepository.save(new Fill(fillId, request.orderId(), fillQuantity, fillPrice));
                    fillSender.send(fill);
                }, delay, TimeUnit.MILLISECONDS);
        }
    }
}