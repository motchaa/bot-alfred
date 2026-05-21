package com.alfred.bot.domain.port.in;

import com.alfred.bot.application.dto.TransactionRequestDTO;
import com.alfred.bot.domain.model.Transaction;

import java.math.BigDecimal;

public interface RegisterExpenseUseCase {
    Transaction execute(TransactionRequestDTO request);
}
