package com.alfred.bot.infrastructure.waha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WahaClient {

    private static final Logger log = LoggerFactory.getLogger(WahaClient.class);
    private final WebClient webClient;

    public WahaClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:3000")
                .defaultHeader("X-Api-Key", "alfred-secret-key")
                .build();
    }

    public void sendTextMessage(String chatId, String text) {
        SendMessageRequest request = new SendMessageRequest();
        request.setChatId(chatId);
        request.setText(text);
        request.setSession("default");

        log.info("📤 Sending message to: {} - Text: {}", chatId, text);

        webClient.post()
                .uri("/api/sendText")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("✅ Message sent successfully: {}", response),
                        error -> log.error("❌ Error sending message: {}", error.getMessage())
                );
    }
}