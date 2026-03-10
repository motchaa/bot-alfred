package com.alfred.bot.application.usecase;

import com.alfred.bot.domain.model.Transaction;
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
        LocalDate now = LocalDate.now();
        BigDecimal balance = transactionRepository.sumAmountByMonthAndYear(now.getMonthValue(), now.getYear());
        return balance != null ? balance : BigDecimal.ZERO;
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
}
