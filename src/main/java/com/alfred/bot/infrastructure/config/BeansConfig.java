package com.alfred.bot.infrastructure.config;

import com.alfred.bot.application.parser.CommandParser;
import com.alfred.bot.application.usecase.CheckBalanceService;
import com.alfred.bot.application.usecase.RegisterExpenseService;
import com.alfred.bot.domain.port.in.CheckBalanceUseCase;
import com.alfred.bot.domain.port.in.RegisterExpenseUseCase;
import com.alfred.bot.domain.port.out.CategoryRepositoryPort;
import com.alfred.bot.domain.port.out.TransactionRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public RegisterExpenseUseCase registerExpenseUseCase(
            TransactionRepositoryPort transactionRepository,
            CategoryRepositoryPort categoryRepository) {
        return new RegisterExpenseService(transactionRepository, categoryRepository);
    }

    @Bean
    public CommandParser commandParser() {
        return new CommandParser();
    }

    @Bean
    public CheckBalanceUseCase checkBalanceUseCase(TransactionRepositoryPort transactionRepository) {
        return new CheckBalanceService(transactionRepository);
    }
}
