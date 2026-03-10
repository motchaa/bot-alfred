package com.alfred.bot.infrastructure.adapter.out.persistence;

import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaTransactionRepository implements TransactionRepositoryPort {
    
    private final SpringDataTransactionRepository repository;

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

        TransactionEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public List<Transaction> findAll() {
        return List.of();
    }

    @Override
    public List<Transaction> findByMonthAndYear(int month, int year) {
        return repository.findByMonthAndYear(month, year).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumAmountByMonthAndYear(int month, int year) {
        BigDecimal total = repository.sumAmountByMonthAndYear(month, year);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Map<String, BigDecimal> sumAmountByCategoryInMonthAndYear(int month, int year) {
        List<Object[]> results = repository.sumAmountByCategoryGrouped(month, year);
        return results.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (BigDecimal) row[1]
        ));
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
