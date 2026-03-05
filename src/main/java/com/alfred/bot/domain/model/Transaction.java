package com.alfred.bot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private TransactionType type;
    private Long categoryId;

}
