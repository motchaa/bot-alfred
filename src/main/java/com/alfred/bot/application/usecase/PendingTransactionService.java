package com.alfred.bot.application.usecase;

import com.alfred.bot.application.dto.TransactionRequestDTO;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingTransactionService {
    private final Map<String, TransactionRequestDTO> pendingTransactions = new ConcurrentHashMap<>();

    public void addPending(String chatId, TransactionRequestDTO request) {
        pendingTransactions.put(chatId, request);
    }

    public Optional<TransactionRequestDTO> getAndRemovePending(String chatId) {
        return Optional.ofNullable(pendingTransactions.remove(chatId));
    }

    public void clearPending(String chatId) {
        pendingTransactions.remove(chatId);
    }
}
