package com.alfred.bot.application.usecase;

import com.alfred.bot.application.dto.TransactionRequestDTO;
import com.alfred.bot.domain.model.Category;
import com.alfred.bot.domain.model.Transaction;
import com.alfred.bot.domain.model.TransactionType;
import com.alfred.bot.domain.port.in.RegisterIncomeUseCase;
import com.alfred.bot.domain.port.out.CategoryRepositoryPort;
import com.alfred.bot.domain.port.out.TransactionRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegisterIncomeService implements RegisterIncomeUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final CategoryRepositoryPort categoryRepository;

    public RegisterIncomeService(TransactionRepositoryPort transactionRepository, CategoryRepositoryPort categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Transaction execute(TransactionRequestDTO request) {
        Long categoryId = null;

        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            Category category = categoryRepository.findByName(request.getCategoryName())
                    .orElseGet(() -> categoryRepository.save(
                            Category.builder().name(request.getCategoryName()).build()
                    ));
            categoryId = category.getId();
        }

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO)
                .description(request.getDescription() != null && !request.getDescription().isBlank() ? request.getDescription() : "Entrada Geral")
                .type(TransactionType.INCOME)
                .categoryId(categoryId)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }
}
