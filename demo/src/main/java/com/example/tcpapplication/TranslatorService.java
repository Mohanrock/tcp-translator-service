package com.example.tcpapplication;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class TranslatorService {

    @Value("${tcp.port:9090}")
    private int tcpPort;

    @Value("${http.endpoint:http://localhost:8081/metrics}")
    private String httpEndpoint;

    private final WebClient webClient;
    private final Queue<DeviceMessage> messageHistory = new ConcurrentLinkedQueue<>();

    public TranslatorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void startServer() {
        TcpServer.create()
                .port(tcpPort)
                .handle((in, out) -> out.sendString(
                        in.receive().asString()
                                .flatMap(this::processAndTranslate)
                ))
                .bind()
                .subscribe();
    }

    public Mono<String> processAndTranslate(String rawData) {
        String data = rawData.trim();
        String[] parts = data.split("\\|");

        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            return Mono.just("ERROR\n");
        }

        DeviceMessage msg = new DeviceMessage(parts[0], parts[1], parts[2]);
        saveToHistory(msg);

        return webClient.post()
                .uri(httpEndpoint)
                .bodyValue(msg)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> "OK\n")
                .retry(1)
                .onErrorReturn("FAILED\n");
    }

    private void saveToHistory(DeviceMessage msg) {
        synchronized (messageHistory) {
            if (messageHistory.size() >= 100) {
                messageHistory.poll();
            }
            messageHistory.offer(msg);
        }
    }

    public List<DeviceMessage> getHistory() {
        return List.copyOf(messageHistory);
    }
}