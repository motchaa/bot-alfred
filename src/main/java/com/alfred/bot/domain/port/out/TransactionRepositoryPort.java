package com.alfred.bot.domain.port.out;

import com.alfred.bot.domain.model.Transaction;

import java.util.List;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    List<Transaction> findAll();
}
