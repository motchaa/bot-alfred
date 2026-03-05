package com.alfred.bot.infrastructure.adapter.out.persistence;

import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.port.out.TransactionRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaTransactionRepository implements TransactionRepositoryPort {
    private final SpringDataTransactionRepository repository;

    public JpaTransactionRepository(SpringDataTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = TransactionEntity.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .type(transaction.getType())
                .categoryId(transaction.getCategoryId())
                .createdAt(transaction.getCreatedAt())
                .build();

        TransactionEntity savedEntity = repository.save(entity);

        return mapToDomain(savedEntity);
    }

    @Override
    public List<Transaction> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Transaction mapToDomain(TransactionEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .type(entity.getType())
                .categoryId(entity.getCategoryId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
