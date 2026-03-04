package com.codace.bot.infrastructure.webhook;

import com.codace.bot.infrastructure.waha.WahaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final WahaClient wahaClient;

    public WebhookController(WahaClient wahaClient) {
        this.wahaClient = wahaClient;
    }

    private String getGreeting() {
        int hour = LocalTime.now().getHour();

        if (hour >= 6 && hour < 12) {
            return "Bom dia";
        } else if (hour >= 12 && hour < 18) {
            return "Boa tarde";
        } else {
            return "Boa noite";
        }
    }

    @PostMapping("/waha")
    public void receiveMessage(@RequestBody WahaWebhookEvent event) {
        log.info("📩 Received webhook event: {}", event.getEvent());


        if ("message".equals(event.getEvent())) {
            String from = event.getPayload().getFrom();
            String text = event.getPayload().getBody();

            log.info("💬 Message from: {} - Text: {}", from, text);

            if (text != null && !text.isEmpty()) {
                String greeting = getGreeting();

                String response = greeting + " senhor, Alfred seu mordomo digital e guardião dos seus ativos financeiros à sua disposição " + "\uD83D\uDC54 \u2615\ufe0f";
                wahaClient.sendTextMessage(from, response);
                log.info("✅ Response sent: {}", response);
            }
        }
    }
}

