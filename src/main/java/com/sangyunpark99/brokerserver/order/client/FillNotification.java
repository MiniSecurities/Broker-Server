package com.sangyunpark99.brokerserver.order.client;

import java.math.BigDecimal;

public record FillNotification(String fillId, Long orderId, Long fillQuantity, BigDecimal fillPrice) {
}
