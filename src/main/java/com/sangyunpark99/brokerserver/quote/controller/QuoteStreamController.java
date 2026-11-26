package com.sangyunpark99.brokerserver.quote.controller;

import com.sangyunpark99.brokerserver.quote.service.QuoteStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteStreamController {

    private final QuoteStreamService quoteStreamService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe() {
        return ResponseEntity.ok(quoteStreamService.subscribe());
    }
}
