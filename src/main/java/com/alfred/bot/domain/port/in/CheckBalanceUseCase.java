package com.alfred.bot.domain.port.in;

import com.alfred.bot.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CheckBalanceUseCase {
    BigDecimal getTotalBalanceForCurrentMonth();

    Map<String, BigDecimal> getExpensesByCategoryForCurrentMonth();

    List<Transaction> getTransactionsForCurrentMonth();

    List<Transaction> getTransactionsByRange(LocalDateTime start, LocalDateTime end);
    
    BalanceSummary getBalanceSummaryForCurrentMonth();

    BalanceSummary getBalanceSummaryByRange(LocalDateTime start, LocalDateTime end);

    public record BalanceSummary(
            BigDecimal totalIncomes,
            BigDecimal totalExpenses,
            BigDecimal currentBalance
    ) {
    }
}
