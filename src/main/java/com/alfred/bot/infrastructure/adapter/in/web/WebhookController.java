
package com.alfred.bot.infrastructure.adapter.in.web;

import com.alfred.bot.application.parser.CommandParser;
import com.alfred.bot.application.parser.CommandType;
import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.model.TransactionType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
                handleCheckBalance(chatId, messageText);
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

    private void handleCheckBalance(String chatId, String text) {
        commandParser.parseMonth(text).ifPresentOrElse(
                month -> {
                    LocalDate targetDate = LocalDate.of(LocalDate.now().getYear(), month, 1);
                    LocalDateTime start = targetDate.atStartOfDay();
                    LocalDateTime end = targetDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

                    String monthName = targetDate.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("pt", "BR"));

                    generateAndSendReport(chatId, start, end, "EXTRATO DE " + monthName.toUpperCase());
                },
                () -> {
                    LocalDate now = LocalDate.now();
                    LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
                    LocalDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

                    generateAndSendReport(chatId, start, end, "EXTRATO MENSAL DETALHADO");
                }
        );
    }

    private void generateAndSendReport(String chatId, LocalDateTime start, LocalDateTime end, String title) {
        try {
            List<Transaction> transactions = checkBalanceUseCase.getTransactionsByRange(start, end);
            CheckBalanceUseCase.BalanceSummary summary = checkBalanceUseCase.getBalanceSummaryByRange(start, end);

            if (transactions.isEmpty()) {
                wahaClient.sendTextMessage(chatId, "📭 *Nenhuma movimentação registrada no período solicitado, senhor ! * ");
                return;
            }

            Map<String, List<Transaction>> grouped = transactions.stream().collect(Collectors.groupingBy(t -> t.getCategoryName() != null ? t.getCategoryName() : "Geral"));

            StringBuilder sb = new StringBuilder("📋 *" + title + "*\n\n");

            grouped.forEach((categoryName, list) -> {
                sb.append(String.format("*%s:*\n", categoryName.toUpperCase()));

                for (Transaction t : list) {
                    String date = t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
                    String icon = (t.getType() == TransactionType.INCOME) ? "📈" : "📉";

                    boolean isIncome = t.getType() == TransactionType.INCOME;
                    String prefix = isIncome ? "[+]" : "[-]";

                    sb.append(String.format("%s %s [%s] *R$ %.2f* - %s\n",
                            icon, prefix, date, t.getAmount(), t.getDescription()));
                }
                sb.append("\n");
            });

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