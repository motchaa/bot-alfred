package com.alfred.bot.infrastructure.adapter.in.web;

import com.alfred.bot.application.parser.CommandParser;
import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.port.in.RegisterExpenseUseCase;
import com.alfred.bot.infrastructure.waha.WahaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WahaClient wahaClient;
    private final RegisterExpenseUseCase registerExpenseUseCase;
    private final CommandParser commandParser;

    public WebhookController(WahaClient wahaClient, RegisterExpenseUseCase registerExpenseUseCase, CommandParser commandParser) {
        this.wahaClient = wahaClient;
        this.registerExpenseUseCase = registerExpenseUseCase;
        this.commandParser = commandParser;
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
    public void handle(@RequestBody WahaWebhookEvent event) {
        // Validação de segurança do evento
        if (event == null || !"message".equals(event.getEvent()) || event.getPayload() ==
                null) {
            return;
        }

        String from = event.getPayload().getFrom();
        String text = event.getPayload().getBody();

        log.info("📩 Mensagem de {}: {}", from, text);

        if (text == null || text.isBlank()) return;

        // O Alfred tenta processar como comando de gasto
        commandParser.parse(text).ifPresentOrElse(
                dto -> {
                    // SUCESSO: É um gasto!
                    Transaction transaction = registerExpenseUseCase.execute(dto);

                    String response = String.format(
                            "Entendido, senhor. Gasto de R$ %.2f em '%s' (%s) devidamente registrado. \uD83D\uDC54 ☕\uFE0F",
                            transaction.getAmount(),
                            transaction.getDescription(),
                            dto.getCategoryName()
                    );

                    wahaClient.sendTextMessage(from, response);
                    log.info("✅ Gasto registrado: {}", response);
                },
                () -> {
                    // FALHA: Apenas papo furado.
                    String greeting = getGreeting();
                    String response = greeting + " senhor. " +
                            "Alfred seu mordomo digital e guardião dos seus ativos financeiros à sua disposição. \uD83D\uDC54 ☕\uFE0F";

                    wahaClient.sendTextMessage(from, response);
                    log.info("👋 Saudação padrão enviada.");
                }
        );
    }
}


