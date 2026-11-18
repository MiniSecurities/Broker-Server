package com.sangyunpark99.brokerserver.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFillService {

    private final FillQuantitySplitter fillQuantitySplitter;

    public void accept(BrokerOrderRequest request) {
        List<Long> fillQuantities = fillQuantitySplitter.split(request.quantity());
    }
}
