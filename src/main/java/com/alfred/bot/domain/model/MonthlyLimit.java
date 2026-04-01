package com.alfred.bot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyLimit {
    private Long id;
    private Integer month;
    private Integer year;
    private BigDecimal amount;
}


