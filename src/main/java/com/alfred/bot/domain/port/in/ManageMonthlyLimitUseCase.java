package com.alfred.bot.domain.port.in;

import com.alfred.bot.domain.model.MonthlyLimit;

import java.math.BigDecimal;

public interface ManageMonthlyLimitUseCase {
    MonthlyLimit setLimit(int month, int year, BigDecimal amount);
    BigDecimal getLimitForMonth(int month, int year);
}
