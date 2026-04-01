package com.alfred.bot.application.parser;

import com.alfred.bot.application.dto.TransactionRequestDTO;

import java.math.BigDecimal;
import java.util.Optional;

public class CommandParser {
    public CommandType getCommandType(String text) {
        if (text == null || text.isBlank()) return CommandType.UNKNOWN;
        String firstWord = text.trim().split("\\s+")[0].toLowerCase();

        return CommandType.fromText(firstWord);
    }

    public Optional<TransactionRequestDTO> parse(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length < 3) return Optional.empty();

        try {
            BigDecimal amount = new BigDecimal(parts[1].replace(",", "."));
            String categoryName = parts.length == 3 ? "Geral" : parts[parts.length - 1];

            String description = extractDescription(parts);

            return Optional.of(new TransactionRequestDTO(amount, description, categoryName));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Integer> parseMonth(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length < 2) return Optional.empty();

        try {
            int month = Integer.parseInt(parts[1]);
            return (month >= 1 && month <= 12) ? Optional.of(month) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String extractDescription(String[] parts) {
        if (parts.length == 3) return parts[2];
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < parts.length - 1; i++) {
          sb.append(parts[i]).append(" ");
        }
        return sb.toString().trim();
    }

    public Optional<BigDecimal> parseLimit(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length < 2) return Optional.empty();
        try {
            return Optional.of(new BigDecimal(parts[1].replace(",", ".")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
