package com.alfred.bot.application.parser;

import java.util.Arrays;

public enum CommandType {
    REGISTER_INCOME("/entrada"),
    REGISTER_EXPENSE("/saida"),
    CHECK_BALANCE("/extrato"),
    UNKNOWN("");

    private final String prefix;

    CommandType(String prefix) {
        this.prefix = prefix;
    }

    public static CommandType fromText(String text) {
        if (text == null || text.isBlank()) return UNKNOWN;

        String firstWord = text.trim().split("\\s+")[0].toLowerCase();

        return Arrays.stream(values())
                .filter(type -> type.prefix.equals(firstWord))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
