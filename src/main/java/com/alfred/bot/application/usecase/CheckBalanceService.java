package com.alfred.bot.application.usecase;

import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.model.TransactionType;
import com.alfred.bot.domain.port.in.CheckBalanceUseCase;
import com.alfred.bot.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CheckBalanceService implements CheckBalanceUseCase {
    private final TransactionRepositoryPort transactionRepository;

    @Override
    public BigDecimal getTotalBalanceForCurrentMonth() {
        return getBalanceSummaryForCurrentMonth().currentBalance();
    }

    @Override
    public Map<String, BigDecimal> getExpensesByCategoryForCurrentMonth() {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        return transactionRepository.sumAmountByCategoryInRange(start, end);
    }

    @Override
    public List<Transaction> getTransactionsForCurrentMonth() {
        LocalDate now = LocalDate.now();

        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();

        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        return transactionRepository.findByRange(startOfMonth, endOfMonth);
    }

    @Override
    public BalanceSummary getBalanceSummaryForCurrentMonth() {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        BigDecimal incomes = transactionRepository.sumAmountByTypeInRange(TransactionType.INCOME, start, end);
        BigDecimal expenses = transactionRepository.sumAmountByTypeInRange(TransactionType.EXPENSE, start, end);

        return new BalanceSummary(incomes != null ? incomes : BigDecimal.ZERO, expenses != null ? expenses : BigDecimal.ZERO, (incomes != null ? incomes : BigDecimal.ZERO).subtract(expenses != null ? expenses
                : BigDecimal.ZERO));
    }
}
