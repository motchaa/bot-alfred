package com.alfred.bot.domain.port.out;

import com.alfred.bot.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    List<Transaction> findAll();
    BigDecimal sumAmountByMonthAndYear(int month, int year);
    Map<String, BigDecimal> sumAmountByCategoryInMonthAndYear(int month, int year);
    List<Transaction> findByMonthAndYear(int month, int year);
}
