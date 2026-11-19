package com.sangyunpark99.brokerserver.order.controller;

import com.sangyunpark99.brokerserver.order.controller.dto.BrokerOrderRequest;
import com.sangyunpark99.brokerserver.order.service.OrderFillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BrokerOrderController {

    private final OrderFillService orderFillService;

    @PostMapping("/api/orders")
    public ResponseEntity<Void> receiveOrder(@Valid @RequestBody BrokerOrderRequest request) {
        orderFillService.accept(request);
        return ResponseEntity.accepted().build();
    }
}
