package com.alfred.bot.application.usecase;

import com.alfred.bot.domain.model.MonthlyLimit;
import com.alfred.bot.domain.port.in.ManageMonthlyLimitUseCase;
import com.alfred.bot.domain.port.out.MonthlyLimitRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ManageMonthlyLimitService implements ManageMonthlyLimitUseCase {
    private final MonthlyLimitRepositoryPort repository;

    @Override
    public MonthlyLimit setLimit(int month, int year, BigDecimal amount) {
        MonthlyLimit limit = repository.findByMonthAndYear(month, year).orElse(MonthlyLimit.builder().month(month).year(year).build());
        limit.setAmount(amount);
        return repository.save(limit);
    }

    @Override
    public BigDecimal getLimitForMonth(int month, int year) {
        return repository.findByMonthAndYear(month, year).map(MonthlyLimit::getAmount).orElse(BigDecimal.ZERO);
    }
}
