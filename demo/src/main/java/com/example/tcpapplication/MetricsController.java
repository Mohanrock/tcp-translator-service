package com.example.tcpapplication;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class MetricsController {
    private final TranslatorService translatorService;

    @GetMapping("/messages")
    public Flux<DeviceMessage> getProcessedMessages() {
        return Flux.fromIterable(translatorService.getHistory());
    }
}