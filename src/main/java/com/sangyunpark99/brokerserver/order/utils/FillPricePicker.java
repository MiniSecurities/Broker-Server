package com.sangyunpark99.brokerserver.order.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class FillPricePicker {

    private static final double MIN_RATIO = 0.98;
    private static final double MAX_RATIO = 1.0;

    public BigDecimal pick(BigDecimal limitPrice) {
        double ratio = ThreadLocalRandom.current().nextDouble(MIN_RATIO, MAX_RATIO);
        BigDecimal fillPrice = limitPrice.multiply(BigDecimal.valueOf(ratio))
                .setScale(2, RoundingMode.DOWN);

        if (fillPrice.signum() <= 0) {
            return limitPrice;
        }
        return fillPrice;
    }
}
