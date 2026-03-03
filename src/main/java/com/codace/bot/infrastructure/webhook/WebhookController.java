package com.codace.bot.infrastructure.webhook;

import com.codace.bot.infrastructure.waha.WahaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final WahaClient wahaClient;

    public WebhookController(WahaClient wahaClient) {
        this.wahaClient = wahaClient;
    }

    @PostMapping("/waha")
    public void receiveMessage(@RequestBody WahaWebhookEvent event) {
        log.info("📩 Received webhook event: {}", event.getEvent());

        if ("message".equals(event.getEvent())) {
            String from = event.getPayload().getFrom();
            String text = event.getPayload().getBody();

            log.info("💬 Message from: {} - Text: {}", from, text);

            // Responder com "Echo: " + mensagem recebida
            if (text != null && !text.isEmpty()) {
                String response = "Olá! Sou o Alfred \uD83E\uDD16 \uD83D\uDC54, seu assistente financeiro virtual " + text;
                wahaClient.sendTextMessage(from, response);
                log.info("✅ Response sent: {}", response);
            }
        }
    }
}