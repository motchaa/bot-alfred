package com.alfred.bot.infrastructure.adapter.in.web;

import com.alfred.bot.application.dto.TransactionRequestDTO;
import com.alfred.bot.application.parser.CommandParser;
import com.alfred.bot.application.parser.CommandType;
import com.alfred.bot.application.usecase.PendingTransactionService;
import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.model.TransactionType;
import com.alfred.bot.domain.port.in.CheckBalanceUseCase;
import com.alfred.bot.domain.port.in.ManageMonthlyLimitUseCase;
import com.alfred.bot.domain.port.in.RegisterExpenseUseCase;
import com.alfred.bot.domain.port.in.RegisterIncomeUseCase;
import com.alfred.bot.infrastructure.waha.WahaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
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
    private final ManageMonthlyLimitUseCase manageMonthlyLimitUseCase;
    private final PendingTransactionService pendingTransactionService;

    @Value("${whatsapp.owner.number}")
    private String ownerNumber;


    @PostMapping
    public void handleWebhook(@RequestBody WahaWebhookEvent event) {
        try {
            if (event == null || event.getPayload() == null) {
                return;
            }

            String chatId = event.getPayload().getFrom();
            String messageText = event.getPayload().getBody();

            log.info("📩 Nova mensagem recebida de: {}", chatId);

            if (!chatId.equals(ownerNumber)) {
                log.warn("⚠️ Acesso não autorizado bloqueado: {}", chatId);
                return;
            }

            if (messageText == null || messageText.isBlank()) return;

            CommandType commandType = commandParser.getCommandType(messageText);
            log.info("🤖 Comando identificado: {}", commandType);

            switch (commandType) {
                case REGISTER_EXPENSE:
                    handleRegisterExpense(chatId, messageText);
                    break;
                case CHECK_BALANCE:
                    handleCheckBalance(chatId, messageText);
                    break;
                case HELP:
                    handleHelp(chatId);
                    break;
                case REGISTER_INCOME:
                    handleRegisterIncome(chatId, messageText);
                    break;
                case SET_LIMIT:
                    handleSetLimit(chatId, messageText);
                    break;
                case CONFIRM_YES:
                    handleConfirmationYes(chatId);
                    break;
                case CONFIRM_NO:
                    handleConfirmationNo(chatId);
                    break;
                default:
                    handleGreeting(chatId);
                    break;
            }
        } catch (Exception e) {
            log.error("❌ ERRO CRÍTICO NO WEBHOOK: ", e);
            throw e; // Mantém o 500 mas agora com o motivo no log
        }
    }

    @org.springframework.web.bind.annotation.GetMapping
    public String healthCheck() {
        return "Alfred is alive and at your service, sir! 🎩";
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

        String message = String.format(greeting + " senhor. Alfred seu mordomo digital e guardião dos seus ativos financeiros à sua disposição. \uD83D\uDC54 ☕\uFE0F");

        wahaClient.sendTextMessage(chatId, message);
    }

    public void handleHelp(String chatId) {
        String helpMessage = """
                *COMANDOS DO ALFRED* \uD83D\uDC54 
                
                    Como seu mordomo digital, aqui estão as tarefas que posso realizar para o senhor:
                
                    💰 */entrada [valor] [descrição]* - Registra um novo aporte.
                    📉 */saida [valor] [descrição] [categoria]* - Registra uma despesa.
                    📊 */extrato* - Exibe o resumo do mês atual.
                    📅 */extrato [mês]* - Exibe o resumo de um mês específico (ex: /extrato 05).
                    🎯 */limite [valor]* - Define seu teto de gastos mensal.
                    ❓ */help* - Exibe esta lista de comandos.
                
                    *Dica:* Ao registrar uma saída, se não informar a categoria, eu utilizarei 'Geral'.
                """;

        wahaClient.sendTextMessage(chatId, helpMessage);
    }

    private void handleSetLimit(String chatId, String text) {
        commandParser.parseLimit(text).ifPresentOrElse(
                amount -> {
                    LocalDate now = LocalDate.now();
                    manageMonthlyLimitUseCase.setLimit(now.getMonthValue(), now.getYear(), amount);
                    String msg = String.format("✅ *Limite definido, senhor.*\n\n💰 *Valor:* R$ %.2f\n📅 *Período:* %d/%d",
                            amount, now.getMonthValue(), now.getYear());
                    wahaClient.sendTextMessage(chatId, msg);
                },
                () -> wahaClient.sendTextMessage(chatId, "⚠️ *Formato Inválido!*\n\nUse: `/limite 1000.00`")
        );
    }

    private void handleRegisterExpense(String chatId, String text) {
        commandParser.parse(text).ifPresentOrElse(
                request -> {
                    if (willExceedLimit(request.getAmount())) {
                        pendingTransactionService.addPending(chatId, request);
                        String alertMsg = String.format(
                                "🚨 *LIMITE EM RISCO, SENHOR!*\n\n" +
                                        "⚠️ Esta saída de *R$ %.2f* ultrapassará seu limite mensal.\n\n" +
                                        "*O senhor realmente deseja ultrapassar seu limite?* (Responda com *Sim* ou *Não*)",
                                request.getAmount()
                        );
                        wahaClient.sendTextMessage(chatId, alertMsg);
                    } else {
                        executeExpenseRegistration(chatId, request);
                    }
                },
                () -> wahaClient.sendTextMessage(chatId, "⚠️ *Formato Inválido!*\n\nUse: `/saida descrição categoria`")
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
                () -> wahaClient.sendTextMessage(chatId, "⚠️ *Formato Inválido!*\n\nUse: `/entrada descrição categoria`")
        );
    }

    private void handleCheckBalance(String chatId, String text) {
        commandParser.parseMonth(text).ifPresentOrElse(
                month -> {
                    LocalDate targetDate = LocalDate.of(LocalDate.now().getYear(), month, 1);
                    LocalDateTime start = targetDate.atStartOfDay();
                    LocalDateTime end = targetDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

                    String monthName = targetDate.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("pt", "BR"));

                    generateAndSendReport(chatId, start, end, "EXTRATO DE " + monthName.toUpperCase(), month, targetDate.getYear());
                },
                () -> {
                    LocalDate now = LocalDate.now();
                    LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
                    LocalDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

                    generateAndSendReport(chatId, start, end, "EXTRATO MENSAL DETALHADO", now.getMonthValue(), now.getYear());
                }
        );
    }

    private void generateAndSendReport(String chatId, LocalDateTime start, LocalDateTime end, String title, int month, int year) {
        try {
            List<Transaction> transactions = checkBalanceUseCase.getTransactionsByRange(start, end);
            CheckBalanceUseCase.BalanceSummary summary = checkBalanceUseCase.getBalanceSummaryByRange(start, end);

            if (transactions.isEmpty()) {
                wahaClient.sendTextMessage(chatId, "📭 *Nenhuma movimentação registrada no período solicitado, senhor!*");
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

            BigDecimal limit = manageMonthlyLimitUseCase.getLimitForMonth(month, year);
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                sb.append(String.format("🎯 *Limite Mensal:* R$ %.2f\n", limit));
                BigDecimal usagePercent = summary.totalExpenses().multiply(new BigDecimal(100)).divide(limit, 1, RoundingMode.HALF_UP);
                sb.append(String.format("📊 *Uso do Limite:* %s%%\n", usagePercent));
            }

            sb.append(String.format("\n💰 *SALDO GERAL: R$ %.2f*", summary.currentBalance()));

            wahaClient.sendTextMessage(chatId, sb.toString());

        } catch (Exception e) {
            log.error("Erro ao gerar extrato: ", e);
            wahaClient.sendTextMessage(chatId, "❌ *Erro ao gerar extrato.*");
        }
    }

    private boolean willExceedLimit(BigDecimal newAmount) {
        LocalDate now = LocalDate.now();
        BigDecimal limit = manageMonthlyLimitUseCase.getLimitForMonth(now.getMonthValue(), now.getYear());

        if (limit.compareTo(BigDecimal.ZERO) <= 0) return false;

        CheckBalanceUseCase.BalanceSummary summary = checkBalanceUseCase.getBalanceSummaryByRange(
                now.withDayOfMonth(1).atStartOfDay(),
                now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
        );
        return summary.totalExpenses().add(newAmount).compareTo(limit) > 0;
    }

    private void handleConfirmationYes(String chatId) {
        pendingTransactionService.getAndRemovePending(chatId).ifPresentOrElse(
                request -> {
                    wahaClient.sendTextMessage(chatId, "🫡 *Entendido, senhor. Limite ignorado desta vez.*");
                    executeExpenseRegistration(chatId, request);
                },
                () -> wahaClient.sendTextMessage(chatId, "❓ *Não encontrei nenhuma transação pendente, senhor.*")
        );
    }

    private void handleConfirmationNo(String chatId) {
        pendingTransactionService.clearPending(chatId);
        wahaClient.sendTextMessage(chatId, "✅ *Operação cancelada. Seus ativos permanecem protegidos, senhor.*");
    }

    private void executeExpenseRegistration(String chatId, TransactionRequestDTO request) {
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

        checkLimitAlert(chatId);
    }

    private void checkLimitAlert(String chatId) {
        LocalDate now = LocalDate.now();
        BigDecimal limit = manageMonthlyLimitUseCase.getLimitForMonth(now.getMonthValue(), now.getYear());

        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
            CheckBalanceUseCase.BalanceSummary summary = checkBalanceUseCase.getBalanceSummaryByRange(start, end);

            BigDecimal totalSpent = summary.totalExpenses();

            if (totalSpent.compareTo(limit) > 0) {
                String alertMsg = String.format(
                        "🚨 *ALERTA DE GASTOS, SENHOR!*\n\n" +
                                "⚠️ O senhor ultrapassou o limite definido para este mês.\n\n" +
                                "🎯 *Limite:* R$ %.2f\n" +
                                "📉 *Gasto Atual:* R$ %.2f\n" +
                                "🚩 *Excesso:* R$ %.2f",
                        limit, totalSpent, totalSpent.subtract(limit)
                );
                wahaClient.sendTextMessage(chatId, alertMsg);
            }
        }
    }
}
