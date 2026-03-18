
package com.alfred.bot.infrastructure.adapter.in.web;

import com.alfred.bot.application.parser.CommandParser;
import com.alfred.bot.application.parser.CommandType;
import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.port.in.CheckBalanceUseCase;
import com.alfred.bot.domain.port.in.RegisterExpenseUseCase;
import com.alfred.bot.domain.port.in.RegisterIncomeUseCase;
import com.alfred.bot.infrastructure.waha.WahaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/webhook/waha")
@RequiredArgsConstructor
public class WebhookController {

    private final CommandParser commandParser;
    private final RegisterExpenseUseCase registerExpenseUseCase;
    private final CheckBalanceUseCase checkBalanceUseCase;
    private final WahaClient wahaClient;
    private final RegisterIncomeUseCase registerIncomeUseCase;


    @PostMapping
    public void handleWebhook(@RequestBody WahaWebhookEvent event) {
        if (event == null || event.getPayload() == null) {
            return;
        }

        String chatId = event.getPayload().getFrom();
        String messageText = event.getPayload().getBody();

        if (messageText == null || messageText.isBlank()) return;

        CommandType commandType = commandParser.getCommandType(messageText);

        switch (commandType) {
            case REGISTER_EXPENSE:
                handleRegisterExpense(chatId, messageText);
                break;

            case CHECK_BALANCE:
                handleCheckBalance(chatId);
                break;

            case REGISTER_INCOME:
                handleRegisterIncome(chatId, messageText);
                break;

            default:
                handleGreeting(chatId);
                break;
        }
    }

    private void handleGreeting(String chatId) {
        LocalTime now = LocalTime.now();
        String greeting;

        if (now.getHour() >= 5 && now.getHour() < 12) {
            greeting = "Bom dia";
        } else if (now.getHour() >= 12 && now.getHour() < 18) {
            greeting = "Boa tarde";
        } else {
            greeting = "Boa noite";
        }

        String message = String.format(greeting + " senhor. " + "Alfred seu mordomo digital e guardião dos seus ativos financeiros à sua disposição. \uD83D\uDC54 ☕\uFE0F");

        wahaClient.sendTextMessage(chatId, message);
    }

    private void handleRegisterExpense(String chatId, String text) {
        commandParser.parse(text).ifPresentOrElse(
                request -> {
                    registerExpenseUseCase.execute(request);
                    String successMsg = String.format(
                            "✅ *Saída registrada, senhor.*\n\n" +
                                    "📝 *Descrição:* %s\n" +
                                    "💰 *Valor:* R$ %.2f\n" +
                                    "🏷️ *Categoria:* %s",
                            request.getDescription(), request.getAmount(),
                            request.getCategoryName()
                    );
                    wahaClient.sendTextMessage(chatId, successMsg);
                },
                () -> wahaClient.sendTextMessage(chatId, "⚠️ *Formato Inválido!*\n\nUse:` /saida descrição categoria`")
        );
    }

    private void handleRegisterIncome(String chatId, String text) {
        commandParser.parse(text).ifPresentOrElse(
                request -> {
                    registerIncomeUseCase.execute(request);
                    String successMsg = String.format(
                            "✅ *Entrada registrada, senhor.*\n\n" +
                                    "📝 *Descrição:* %s\n" +
                                    "💰 *Valor:* R$ %.2f\n" +
                                    "🏷️ *Categoria:* %s",
                            request.getDescription(), request.getAmount(),
                            request.getCategoryName()
                    );
                    wahaClient.sendTextMessage(chatId, successMsg);
                },
                () -> wahaClient.sendTextMessage(chatId, "⚠️ *Formato Inválido!*\n\nUse:` /entrada descrição categoria`")
        );
    }

    private void handleCheckBalance(String chatId) {
        try {
            CheckBalanceUseCase.BalanceSummary summary = checkBalanceUseCase.getBalanceSummaryForCurrentMonth();
            List<Transaction> transactions =
                    checkBalanceUseCase.getTransactionsForCurrentMonth();

            if (transactions.isEmpty()) {
                wahaClient.sendTextMessage(chatId, "📭 *Nenhuma movimentação registrada este mês, chefe ! * ");
                return;
            }

            StringBuilder sb = new StringBuilder("📋 *EXTRATO MENSAL DETALHADO* \n\n");

            for (Transaction t : transactions) {
                String date = t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
                String icon = (t.getType() ==
                        com.alfred.bot.domain.model.TransactionType.INCOME) ? "📈" : "📉";

                boolean isIncome = t.getType() == com.alfred.bot.domain.model.TransactionType.INCOME;
                String prefix = isIncome ? "[+]" : "[-]";

                sb.append(String.format("%s %s [%s] *R$ %.2f* - %s\n",
                        icon,prefix, date, t.getAmount(), t.getDescription()));
            }

            sb.append("\n─────────────────\n");
            sb.append(String.format("📈 *Total de Entradas:* R$ %.2f\n", summary.totalIncomes()));
            sb.append(String.format("📉 *Total de Saídas:* R$ %.2f\n", summary.totalExpenses()));
            sb.append(String.format("\n💰 *SALDO GERAL: R$ %.2f*", summary.currentBalance()));

            wahaClient.sendTextMessage(chatId, sb.toString());

        } catch (Exception e) {
            log.error("Erro ao gerar extrato: ", e);
            wahaClient.sendTextMessage(chatId, "❌ *Erro ao gerar extrato.*");
        }
    }
}