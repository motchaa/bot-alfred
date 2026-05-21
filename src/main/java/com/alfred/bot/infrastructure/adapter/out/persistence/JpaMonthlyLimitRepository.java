package com.alfred.bot.infrastructure.adapter.out.persistence;

import com.alfred.bot.domain.model.MonthlyLimit;
import com.alfred.bot.domain.port.out.MonthlyLimitRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaMonthlyLimitRepository implements MonthlyLimitRepositoryPort {

    private final SpringDataMonthlyLimitRepository repository;

    @Override
    public MonthlyLimit save(MonthlyLimit monthlyLimit) {
        MonthlyLimitEntity entity = MonthlyLimitEntity.builder()
                .id(monthlyLimit.getId())
                .month(monthlyLimit.getMonth())
                .year(monthlyLimit.getYear())
                .amount(monthlyLimit.getAmount())
                .build();

        MonthlyLimitEntity savedEntity = repository.save(entity);

        monthlyLimit.setId(savedEntity.getId());
        return monthlyLimit;
    }

    @Override
    public Optional<MonthlyLimit> findByMonthAndYear(int month, int year) {
        return repository.findByMonthAndYear(month, year)
                .map(entity -> MonthlyLimit.builder()
                        .id(entity.getId())
                        .month(entity.getMonth())
                        .year(entity.getYear())
                        .amount(entity.getAmount())
                        .build());
    }
}
