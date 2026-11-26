package com.sangyunpark99.brokerserver.quote.service;

import com.sangyunpark99.brokerserver.quote.Quote;
import com.sangyunpark99.brokerserver.quote.repository.QuoteEmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class QuoteStreamService {

    private static final long TIMEOUT_MILLIS = Duration.ofMinutes(30).toMillis();

    private final QuoteEmitterRepository quoteEmitterRepository;

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);

        quoteEmitterRepository.add(emitter);

        emitter.onCompletion(() -> quoteEmitterRepository.remove(emitter));
        emitter.onTimeout(() -> quoteEmitterRepository.remove(emitter));
        emitter.onError(e -> quoteEmitterRepository.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("연결됨"));
        } catch (IOException e) {
            quoteEmitterRepository.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(Quote quote) {
        for(SseEmitter emitter: quoteEmitterRepository.findAll()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("quote")
                        .id(String.valueOf(quote.sequence()))
                        .data(quote, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException e) {
                quoteEmitterRepository.remove(emitter);
            }
        }
    }
}
