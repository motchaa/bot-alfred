package com.alfred.bot.domain.port.in;

import com.alfred.bot.application.dto.TransactionRequestDTO;
import com.alfred.bot.domain.model.Transaction;

public interface RegisterIncomeUseCase {
    Transaction execute(TransactionRequestDTO request);
}
