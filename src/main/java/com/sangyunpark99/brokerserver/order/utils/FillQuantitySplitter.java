package com.sangyunpark99.brokerserver.order.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class FillQuantitySplitter {

    public List<Long> split(long quantity) {

        List<Long> fillQuantities = new ArrayList<>();
        long remaining = quantity;

        while (remaining > 0) {
            long fillQuantity = ThreadLocalRandom.current().nextLong(1, remaining + 1);
            remaining -= fillQuantity;
            fillQuantities.add(fillQuantity);
        }

        return fillQuantities;
    }
}
