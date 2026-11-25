package com.sangyunpark99.brokerserver.fill;

import com.sangyunpark99.brokerserver.fill.repository.FillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FillScheduler {

    private final FillRepository fillRepository;
    private final FillSender fillSender;

    @Scheduled(fixedDelay = 200)
    public void fill() {
        List<Fill> dueFills = fillRepository.findTop100ByStatusAndFillAtLessThanEqualOrderByIdAsc(
                FillStatus.SCHEDULED,
                LocalDateTime.now()
        );

        for(Fill fill: dueFills) {
            fill.markFilled();
            fillRepository.save(fill);
            fillSender.send(fill);
        }
    }

}
