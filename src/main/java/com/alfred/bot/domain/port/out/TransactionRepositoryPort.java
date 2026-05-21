package com.alfred.bot.domain.port.out;

import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    List<Transaction> findAll();
    BigDecimal sumAmountByTypeInRange(TransactionType type, LocalDateTime start, LocalDateTime end);
    Map<String, BigDecimal> sumAmountByCategoryInRange(LocalDateTime start, LocalDateTime end);
    List<Transaction> findByRange(LocalDateTime start, LocalDateTime end);
}
