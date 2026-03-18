package com.alfred.bot.application.usecase;

import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.model.TransactionType;
import com.alfred.bot.domain.port.in.CheckBalanceUseCase;
import com.alfred.bot.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
        return transactionRepository.sumAmountByCategoryInMonthAndYear(now.getMonthValue(), now.getYear());
    }

    @Override
    public List<Transaction> getTransactionsForCurrentMonth() {
        LocalDate now = LocalDate.now();
        return transactionRepository.findByMonthAndYear(now.getMonthValue(), now.getYear());
    }

    @Override
    public BalanceSummary getBalanceSummaryForCurrentMonth() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        BigDecimal incomes = transactionRepository.sumAmountByTypeAndMonthAndYear(TransactionType.INCOME, month, year);
        BigDecimal expenses = transactionRepository.sumAmountByTypeAndMonthAndYear(TransactionType.EXPENSE, month, year);

        return new BalanceSummary(incomes != null ? incomes : BigDecimal.ZERO, expenses != null ? expenses : BigDecimal.ZERO, (incomes != null ? incomes : BigDecimal.ZERO).subtract(expenses != null ? expenses
                : BigDecimal.ZERO));
    }
}
