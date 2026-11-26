package com.sangyunpark99.brokerserver.quote.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuoteEmitterRepository {

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public void add(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void remove(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public Set<SseEmitter> findAll() {
        return emitters;
    }
}
