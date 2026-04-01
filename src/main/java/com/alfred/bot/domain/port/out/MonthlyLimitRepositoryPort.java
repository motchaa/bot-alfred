package com.alfred.bot.domain.port.out;

import com.alfred.bot.domain.model.MonthlyLimit;

import java.util.Optional;

public interface MonthlyLimitRepositoryPort {
    MonthlyLimit save(MonthlyLimit monthlyLimit);
    Optional<MonthlyLimit> findByMonthAndYear(int month, int year);
}
