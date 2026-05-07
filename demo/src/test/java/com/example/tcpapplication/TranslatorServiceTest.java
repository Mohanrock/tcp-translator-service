package com.example.tcpapplication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslatorServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private TranslatorService translatorService;

    @BeforeEach
    void setUp() {

        when(webClientBuilder.build()).thenReturn(webClient);

        translatorService = new TranslatorService(webClientBuilder);

        ReflectionTestUtils.setField(
                translatorService,
                "httpEndpoint",
                "http://localhost:8081/metrics"
        );
    }

    @Test
    void processAndTranslate_ShouldReturnOk_WhenValidInput() {

        when(webClient.post()).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        when(requestBodySpec.bodyValue(any(DeviceMessage.class)))
                .thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("OK"));

        Mono<String> result =
                translatorService.processAndTranslate("device1|temp|25");

        StepVerifier.create(result)
                .expectNext("OK\n")
                .verifyComplete();

        List<DeviceMessage> history = translatorService.getHistory();

        assertEquals(1, history.size());
        assertEquals("device1", history.getFirst().deviceId());
    }

    @Test
    void processAndTranslate_ShouldReturnError_WhenInvalidInput() {

        Mono<String> result = translatorService.processAndTranslate("invalid-data");

        StepVerifier.create(result)
                .expectNext("ERROR\n")
                .verifyComplete();

        assertEquals(0, translatorService.getHistory().size());

        verifyNoInteractions(webClient);
    }

    @Test
    void processAndTranslate_ShouldReturnFailed_WhenHttpCallFails() {

        when(webClient.post()).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        when(requestBodySpec.bodyValue(any(DeviceMessage.class)))
                .thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("HTTP Error")));

        Mono<String> result =
                translatorService.processAndTranslate("device1|temp|25");

        StepVerifier.create(result)
                .expectNext("FAILED\n")
                .verifyComplete();
    }

    @Test
    void getHistory_ShouldMaintainMax100Messages() {

        when(webClient.post()).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        when(requestBodySpec.bodyValue(any(DeviceMessage.class)))
                .thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("OK"));

        for (int i = 1; i <= 101; i++) {
            translatorService.processAndTranslate(
                    "device" + i + "|temp|" + i
            ).block();
        }

        List<DeviceMessage> history = translatorService.getHistory();

        assertEquals(100, history.size());

        assertEquals("device2", history.getFirst().deviceId());
    }
}