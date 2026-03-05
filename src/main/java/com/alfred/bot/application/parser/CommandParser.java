package com.alfred.bot.application.parser;

import com.alfred.bot.application.dto.TransactionRequestDTO;

import java.math.BigDecimal;
import java.util.Optional;

public class CommandParser {
    public Optional<TransactionRequestDTO> parse(String text) {
        if(text == null || text.isBlank()) {
            return Optional.empty();
        }

        String[] parts = text.trim().split("\\s+");

        if(parts.length < 3) {
            return Optional.empty();
        }

        String command = parts[0].toLowerCase();
        if(!"/gasto".equals(command)) {
            return Optional.empty();
        }

        try {
            String amountStr = parts[1].replace(",",".");
            BigDecimal amount = new BigDecimal(amountStr);

            String description;
            String categoryName;

            if (parts.length == 3) {
                description = parts[2];
                categoryName = "Geral";
            } else {
                categoryName = parts[parts.length - 1];

                StringBuilder sb = new StringBuilder();

                for(int i = 2; i < parts.length - 1; i++) {
                    sb.append(parts[i]).append(" ");
                }

                description = sb.toString().trim();
            }

            return Optional.of(new TransactionRequestDTO(amount, description, categoryName));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
