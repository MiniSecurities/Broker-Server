package com.sangyunpark99.brokerserver.order.client;

import java.math.BigDecimal;

public record FillNotification(Long orderId, Long fillQuantity, BigDecimal fillPrice) {
}
