package com.example.tcpapplication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    @Mock
    private TranslatorService translatorService;

    @InjectMocks
    private MetricsController metricsController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToController(metricsController)
                .build();
    }

    @Test
    void shouldReturnProcessedMessages() {

        DeviceMessage message1 = new DeviceMessage("101", "temp","67");

        DeviceMessage message2 = new DeviceMessage("102", "temp","32");

        when(translatorService.getHistory())
                .thenReturn(List.of(message1, message2));

        webTestClient.get()
                .uri("/messages")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DeviceMessage.class)
                .hasSize(2);
    }
}