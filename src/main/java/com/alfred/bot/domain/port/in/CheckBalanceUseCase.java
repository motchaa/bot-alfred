package com.alfred.bot.domain.port.in;

import com.alfred.bot.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CheckBalanceUseCase {
    BigDecimal getTotalBalanceForCurrentMonth();
    Map<String, BigDecimal> getExpensesByCategoryForCurrentMonth();
    List<Transaction> getTransactionsForCurrentMonth();
}
