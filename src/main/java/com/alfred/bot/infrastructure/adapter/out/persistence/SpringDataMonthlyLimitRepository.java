package com.alfred.bot.infrastructure.adapter.out.persistence;

import com.alfred.bot.domain.model.MonthlyLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataMonthlyLimitRepository extends JpaRepository<MonthlyLimitEntity, Long> {
    Optional<MonthlyLimitEntity> findByMonthAndYear(Integer month, Integer year);
}
